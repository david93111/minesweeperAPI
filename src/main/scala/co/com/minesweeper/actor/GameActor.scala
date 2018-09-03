package co.com.minesweeper.actor

import akka.actor.Props
import co.com.minesweeper.actor.GameActor.{GetMinefield, MarkSpot, MinefieldOperation, RevealSpot}
import co.com.minesweeper.api.services.MinefieldService
import co.com.minesweeper.model._
import co.com.minesweeper.model.error.GameOperationFailed

class GameActor(val id: String, minefieldConf: MinefieldConfig, val user: String) extends BaseActor {

  val minefield: Minefield = MinefieldService.createMinefield(minefieldConf.columns, minefieldConf.rows, minefieldConf.mines)

  var status: GameStatus = GameStatus.Active

  private val cellsToReveal: Int = (minefieldConf.rows * minefieldConf.columns) - minefieldConf.mines

  private var revealedCells: Int = 0

  val revealFunction: (Int, Int, Field) => Unit = MinefieldService.revealSpot(minefield.board)

  private val validateCellOperation: (Int, Int) => Boolean = MinefieldService.cellInValidRange(minefieldConf.columns, minefieldConf.rows)

  def minefieldOperationsReceive: Receive = {
    case RevealSpot(row, col) =>
      val field: Field = minefield.board(row)(col)
      revealSpot(row, col, field)
      field.fieldType match {
        case FieldType.Mine =>
          status = GameStatus.Lose
        case FieldType.Hint =>
          revealedCells = revealedCells + 1
          validateIfWon()
        case FieldType.Empty =>
          val quantityRevealed =  MinefieldService.revealSpotsUntilHintOrMine(minefield.board, MinefieldService.nearSpots(row, col),validateCellOperation)
          revealedCells = revealedCells + 1 + quantityRevealed
          validateIfWon()
      }
      sender() ! Game(id, status, user, minefield)
    case MarkSpot(row, col, mark) =>
      val field: Field = minefield.board(row)(col)
      if (!field.revealed || mark == MarkType.None){
        minefield.board(row)(col) = field.copy(mark = mark)
        sender() ! Game(id, status, user, minefield)
      } else sender() ! GameOperationFailed.cellAlreadyRevealed(id)
  }

  override def receive: Receive = {
    case GetMinefield =>
      sender() ! Game(id, status, user, minefield)
    case e: MinefieldOperation =>
      if(!validateCellOperation(e.row, e.column))
        sender() ! GameOperationFailed.invalidCell(id)
      else if(status != GameStatus.Active)
        sender() ! GameOperationFailed.GameFinished(id)
      else
        minefieldOperationsReceive(e)
  }

  private def validateIfWon(): Unit = {
    if(revealedCells == cellsToReveal) status = GameStatus.Won
  }

  private def revealSpot(row: Int, col: Int, field: Field): Unit ={
    revealFunction(row, col, field)
  }
}

object GameActor{

  case object GetMinefield
  abstract class MinefieldOperation(val row: Int, val column: Int)
  case class RevealSpot(override val row: Int, override val column: Int) extends MinefieldOperation(row, column)
  case class MarkSpot(override val row: Int, override val column: Int, mark: MarkType) extends MinefieldOperation(row, column)

  def props( id: String, minefieldConf: MinefieldConfig, user: String): Props = {
    Props(new GameActor(id, minefieldConf, user))
  }

}
