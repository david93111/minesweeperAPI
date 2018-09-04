package co.com.minesweeper.actor

import akka.actor.Props
import akka.persistence.{PersistentActor, SnapshotOffer}
import co.com.minesweeper.actor.GameActor.{GetMinefield, MarkSpot, MinefieldOperation, RevealSpot}
import co.com.minesweeper.api.services.MinefieldService
import co.com.minesweeper.model._
import co.com.minesweeper.model.error.GameOperationFailed

class GameActor(val id: String, minefieldConf: MinefieldConfig, val user: String) extends PersistentActor with BaseActor {

  override def persistenceId: String = id

  var state = Game(id, GameStatus.Active, user, MinefieldService.createMinefield(minefieldConf.columns, minefieldConf.rows, minefieldConf.mines))

  persistAsync(state)
  saveSnapshot(state)
  if (lastSequenceNr % GameActor.snapShotInterval == 0 && lastSequenceNr != 0)
    saveSnapshot(state)

  var movementHistory: List[Game] = List.empty[Game]

  private val cellsToReveal: Int = (minefieldConf.rows * minefieldConf.columns) - minefieldConf.mines

  private var revealedCells: Int = 0

  val revealFunction: (Int, Int, Field) => Unit = MinefieldService.revealSpot(state.minefield.board)

  private val validateCellOperation: (Int, Int) => Boolean = MinefieldService.cellInValidRange(minefieldConf.columns, minefieldConf.rows)

  def minefieldOperationsReceive: Receive = {
    case RevealSpot(row, col) =>
      val field: Field = state.minefield.board(row)(col)
      revealSpot(row, col, field)
      field.fieldType match {
        case FieldType.Mine =>
          updateState(GameStatus.Lose)
        case FieldType.Hint =>
          revealedCells = revealedCells + 1
          validateIfWon()
        case FieldType.Empty =>
          val quantityRevealed = MinefieldService.revealSpotsUntilHintOrMine(state.minefield.board, MinefieldService.nearSpots(row, col),validateCellOperation)
          revealedCells = revealedCells + 1 + quantityRevealed
          validateIfWon()
      }
      sender() ! state
    case MarkSpot(row, col, mark) =>
      val field: Field = state.minefield.board(row)(col)
      if (!field.revealed || mark == MarkType.None){
        state.minefield.board(row)(col) = field.copy(mark = mark)
        sender() ! state
      } else sender() ! GameOperationFailed.cellAlreadyRevealed(id)
  }

  override def receiveRecover: Receive = {
    case game: Game                                 ⇒ updateState(game)
    case SnapshotOffer(_, snapshot: ) ⇒ state = snapshot
  }

  override def receiveCommand: Receive = {
    case GetMinefield =>
      sender() ! state
    case e: MinefieldOperation =>
      if(!validateCellOperation(e.row, e.column))
        sender() ! GameOperationFailed.invalidCell(id)
      else if(state.gameStatus != GameStatus.Active)
        sender() ! GameOperationFailed.GameFinished(id)
      else
        minefieldOperationsReceive(e)
  }

  private def updateState(gameStatus: GameStatus): Unit ={
      movementHistory = state :: movementHistory
      state = state.copy(gameStatus = gameStatus)
  }

  private def uptadeState(newState: Game): Unit = {
      movementHistory = state :: movementHistory
      state = newState
  }

  private def validateIfWon(): Unit = {
    if(revealedCells == cellsToReveal) updateState(GameStatus.Won)
  }

  private def revealSpot(row: Int, col: Int, field: Field): Unit ={
    revealFunction(row, col, field)
  }



}

object GameActor{

  val snapShotInterval = 1000
  case object GetMinefield
  abstract class MinefieldOperation(val row: Int, val column: Int)
  case class RevealSpot(override val row: Int, override val column: Int) extends MinefieldOperation(row, column)
  case class MarkSpot(override val row: Int, override val column: Int, mark: MarkType) extends MinefieldOperation(row, column)

  case class GameState(movementHistory: List[Game]){

  }

  def props( id: String, minefieldConf: MinefieldConfig, user: String): Props = {
    Props(new GameActor(id, minefieldConf, user))
  }

}
