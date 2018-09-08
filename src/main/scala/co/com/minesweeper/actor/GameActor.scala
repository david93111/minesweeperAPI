package co.com.minesweeper.actor

import akka.actor.{Props, ReceiveTimeout}
import akka.persistence.{PersistentActor, Recovery, SnapshotOffer, SnapshotSelectionCriteria}
import co.com.minesweeper.actor.GameActor._
import co.com.minesweeper.api.services.MinefieldService
import co.com.minesweeper.config.AppConf
import co.com.minesweeper.model._
import co.com.minesweeper.model.error.GameOperationFailed
import co.com.minesweeper.model.messages.{GameBasicOperation, GameHistory, GameState}
import co.com.minesweeper.util.Timer

/**  Representation of a game through an persistent actor
  *
  *  @author david93111
  *
  *  Implements basic persistenct actor with no tag behavior over messages for now
  *
  *  As a benefit of persist game as actor state, event sourcing is used over the game operations and snapshot as
  *  a complete history of events over the actor/game
  *
  * */
class GameActor(val id: String, currentGame: GameState) extends PersistentActor with BaseActor {

  context.setReceiveTimeout(AppConf.gameActorMaxIdleTime)

  override def persistenceId: String = id

  var state: GameState = currentGame

  val timer: Timer = Timer.createTimer(state.timerInSeconds)

  if(state.gameStatus ==  GameStatus.Active){
    timer.start()
  }

  var gameHistory: GameHistory = GameHistory()

  val cellsToReveal: Int = (state.minefield.rows * state.minefield.columns) - state.minefield.mines

  val validateCellOperation: (Int, Int) => Boolean = MinefieldService.cellInValidRange(state.minefield.columns, state.minefield.rows)

  /** segment receive only focused on minefield operations: Revel and Mark */
  def minefieldOperationsReceive: Receive = {
    case reveal: RevealSpot=>
      val field: Field = state.minefield.board(reveal.row)(reveal.column)
      if(field.revealed){
        sender() ! GameOperationFailed.cellAlreadyRevealed(id)
      }else{
        revealSpot(reveal.row, reveal.column, field)
        field.fieldType match {
          case FieldType.Mine =>
            updateState(GameStatus.Lose, state.revealedCells + 1, reveal.toString, timer.stop())
          case FieldType.Hint =>
            validateIfWon(state.revealedCells + 1, reveal.toString)
          case FieldType.Empty =>
            val quantityRevealed = MinefieldService.revealSpotsUntilHintOrMine(state.minefield.board, MinefieldService.nearSpots(reveal.row, reveal.column),validateCellOperation)
            val newRevealed: Int = state.revealedCells + quantityRevealed + 1
            validateIfWon(newRevealed, reveal.toString)
        }
        sender() ! state
      }
    case mark : MarkSpot =>
      val field: Field = state.minefield.board(mark.row)(mark.column)
      if (!field.revealed || mark.mark == MarkType.None){
        state.minefield.board(mark.row)(mark.column) = field.copy(mark = mark.mark)
        updateState(mark.toString, timer.elapsed())
        sender() ! state
      } else sender() ! GameOperationFailed.cellAlreadyRevealed(id)
  }

  def timerOperationReceive: Receive ={
    case PauseGame =>
      if(!state.paused){
        updateState(PauseGame.toString, timer.stop(), paused = true)
      }
      sender() ! state
    case ResumeGame =>
      if(state.paused){
        timer.start()
        updateState(ResumeGame.toString, timer.elapsed())
      }
      sender() ! state
  }

  /** Explicit recovery strategy */
  override def recovery = Recovery(fromSnapshot = SnapshotSelectionCriteria.Latest)

  /** Persistence recovery, the GameManager should already take care of find the last journal
    * and review if the event may be functional, but this is needed in case that actor crashed caused by an exception
    **/
  override def receiveRecover: Receive = {
    case game: GameState =>
      logger.info(s"loading game state trough recover, adding to historic: state -> {}", game)
      state = state
      gameHistory = gameHistory.update(game)
    case SnapshotOffer(_, snapshot: GameHistory ) =>
      logger.info(s"loading snapshot trough recover, adding to historic: snapshot -> {}", snapshot)
      gameHistory = snapshot
      if(gameHistory.history.nonEmpty){
        state = gameHistory.history.head
      }
  }

  /** Required receive command por persistent actor, manage and validate minefield operations */
  override def receiveCommand: Receive = {
    case Snap =>
      snap()
    case GetGameState =>
      logger.debug("Message of get minefield received, pulling historic {}", gameHistory.toString)
      sender() ! state
    case e: MinefieldOperation =>
      logger.debug("Processing Message of operation, content: {}", e.toString)
      if(state.gameStatus != GameStatus.Active || state.paused)
        sender() ! GameOperationFailed.GameFinishedOrPaused(id)
      else if(!validateCellOperation(e.row, e.column))
        sender() ! GameOperationFailed.invalidCell(id)
      else
        minefieldOperationsReceive(e)
    case message: TimerOperation =>
      if(state.gameStatus != GameStatus.Active)
        sender() ! GameOperationFailed.GameFinished(id)
      else timerOperationReceive(message)
    case GetHistory =>
      sender() ! gameHistory
    case ReceiveTimeout | AutoShutdown => // Stop the actor if idle (no messages received) time of actor surpass max idle time
      logger.info(s"Stoping actor $id, has surpassed max idle time")
      if(!state.paused){
        autoPauseAndStopAfterIdle("AutoPaused")
      }else{
        context.stop(self)
      }
  }

  /** centralized way of persist journal events and if success update state and take snapshot for easy recovery */
  def snap(): Unit = {
      persist(state) { result =>
        logger.info(s"Journal persist successful for game: ${state.gameId} was: $result")
        gameHistory = gameHistory.update(result)
        saveSnapshot(gameHistory)
      }
  }


  /** Different possibilities of state change management
    *
    * As state of the game came change im multiple ways, only partial modification of state is needed
    * and all this partial changes are managed on this section
    *
    * */
  private def autoPauseAndStopAfterIdle(action: String): Unit ={
    state = state.copy(timerInSeconds = timer.stop(), paused = true, lastAction = action)
    snap()
    context.stop(self)
  }

  private def updateState(action: String, elapsed: Long, paused: Boolean = false): Unit ={
    state = state.copy(lastAction = action, timerInSeconds = timer.elapsed(), paused = paused)
    snap()
  }

  private def updateState(revealedCells: Int, action: String): Unit ={
    state = state.copy( revealedCells = revealedCells, lastAction = action , timerInSeconds = timer.elapsed())
    snap()
  }

  private def updateState(gameStatus: GameStatus, revealedCells: Int, action: String, elapsed: Long): Unit ={
    state = state.copy(gameStatus = gameStatus, revealedCells = revealedCells, lastAction = action , timerInSeconds = elapsed)
    snap()
  }

  private def validateIfWon(revealedCells: Int, action: String): Unit = {
    if(state.revealedCells + revealedCells == cellsToReveal){
      updateState(GameStatus.Won, revealedCells, action, timer.stop())
    }else{
      updateState(revealedCells, action)
    }
  }

  private def revealSpot(row: Int, col: Int, field: Field): Unit ={
    MinefieldService.revealSpot(state.minefield.board)(row, col, field)
  }

}

/** Factory for GameActor Props, specify the available DSL with the actor  */
object GameActor{

  case object GetGameState
  abstract class MinefieldOperation(val row: Int, val column: Int)
  case class RevealSpot(override val row: Int, override val column: Int) extends MinefieldOperation(row, column)
  case class MarkSpot(override val row: Int, override val column: Int, mark: MarkType) extends MinefieldOperation(row, column)

  trait TimerOperation extends GameBasicOperation

  case object PauseGame extends TimerOperation
  case object ResumeGame extends TimerOperation
  case object GetHistory extends GameBasicOperation

  // Class to avoid use of poson pill
  case object AutoShutdown

  case object Snap

  def props( id: String, game: GameState): Props = {
    Props(new GameActor(id, game))
  }

}
