package co.com.minesweeper.actor

import akka.actor.Props
import akka.persistence.{PersistentActor, SnapshotOffer}
import co.com.minesweeper.actor.GameActor._
import co.com.minesweeper.api.services.MinefieldService
import co.com.minesweeper.model._
import co.com.minesweeper.model.error.GameOperationFailed

class GameActor(val id: String, currentGame: GameState) extends PersistentActor with BaseActor {

  override def persistenceId: String = id

  var state: GameState = currentGame

  var gameHistory: GameActor.GameHistory = GameHistory()

  val cellsToReveal: Int = (state.minefield.rows * state.minefield.columns) - state.minefield.mines

  val validateCellOperation: (Int, Int) => Boolean = MinefieldService.cellInValidRange(state.minefield.columns, state.minefield.columns)

  def minefieldOperationsReceive: Receive = {
    case reveal: RevealSpot=>
      val field: Field = state.minefield.board(reveal.row)(reveal.column)
      if(field.revealed){
        sender() ! GameOperationFailed.cellAlreadyRevealed(id)
      }else{
        revealSpot(reveal.row, reveal.column, field)
        field.fieldType match {
          case FieldType.Mine =>
            updateState(GameStatus.Lose, reveal.toString)
          case FieldType.Hint =>
            val newRevealed: Int = state.revealedCells + 1
            validateIfWon(newRevealed, reveal.toString)
          case FieldType.Empty =>
            val quantityRevealed = MinefieldService.revealSpotsUntilHintOrMine(state.minefield.board, MinefieldService.nearSpots(reveal.row, reveal.column),validateCellOperation)
            val newRevealed: Int = state.revealedCells + quantityRevealed
            validateIfWon(newRevealed, reveal.toString)
        }
        sender() ! state
      }
    case mark : MarkSpot =>
      val field: Field = state.minefield.board(mark.row)(mark.column)
      if (!field.revealed || mark.mark == MarkType.None){
        state.minefield.board(mark.row)(mark.column) = field.copy(mark = mark.mark)
        updateState(mark.toString)
        sender() ! state
      } else sender() ! GameOperationFailed.cellAlreadyRevealed(id)
  }

  override def receiveRecover: Receive = {
    case game: GameState =>
      state = game
      gameHistory = gameHistory.update(game)
    case SnapshotOffer(_, snapshot: GameHistory ) =>
      gameHistory = snapshot
  }

  override def receiveCommand: Receive = {
    case Snap =>
      snap()
    case GetMinefield =>
      logger.debug("Message of get minefield received, pulling historic {}", gameHistory.toString)
      sender() ! state
    case e: MinefieldOperation =>
      logger.debug("Processing Message of operation, content: {}", e.toString)
      if(!validateCellOperation(e.row, e.column))
        sender() ! GameOperationFailed.invalidCell(id)
      else if(state.gameStatus != GameStatus.Active)
        sender() ! GameOperationFailed.GameFinished(id)
      else
        minefieldOperationsReceive(e)
  }

  def snap(): Unit = {
      gameHistory = gameHistory.update(state)
      saveSnapshot()
      persist(state)(result => logger.info(s"Journal persist result for game: ${state.gameId} was: $result"))
  }

  private def updateState(gameStatus: GameStatus, action: String): Unit ={
      state = state.copy(gameStatus = gameStatus, lastAction = action )
      snap()
  }

  private def updateState(action: String): Unit ={
    state = state.copy(lastAction = action)
    snap()
  }

  private def updateState(revealedCells: Int, action: String): Unit ={
    state = state.copy( revealedCells = revealedCells, lastAction = action )
    snap()
  }

  private def updateState(gameStatus: GameStatus, revealedCells: Int, action: String): Unit ={
    state = state.copy(gameStatus = gameStatus, revealedCells = revealedCells, lastAction = action )
    snap()
  }

  private def validateIfWon(revealedCells: Int, action: String): Unit = {
    if(revealedCells == cellsToReveal){
      updateState(GameStatus.Won, revealedCells, action)
    }else{
      updateState(revealedCells,action)
    }
  }

  private def revealSpot(row: Int, col: Int, field: Field): Unit ={
    MinefieldService.revealSpot(state.minefield.board)(row, col, field)
  }

}

object GameActor{

  val snapShotInterval = 1000
  case object GetMinefield
  abstract class MinefieldOperation(val row: Int, val column: Int)
  case class RevealSpot(override val row: Int, override val column: Int) extends MinefieldOperation(row, column)
  case class MarkSpot(override val row: Int, override val column: Int, mark: MarkType) extends MinefieldOperation(row, column)
  case object Snap

  sealed case class GameHistory(historic: List[GameState] = Nil){
      def update(move: GameState): GameHistory = {
          copy(move::historic)
      }
      def getLastMovement: Option[GameState] = historic.headOption
    override def toString: String = historic.reverse.mkString("|-|")
  }

  def props( id: String, game: GameState): Props = {
    Props(new GameActor(id, game))
  }

}
