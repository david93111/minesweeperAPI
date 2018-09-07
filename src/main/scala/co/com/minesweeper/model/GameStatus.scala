package co.com.minesweeper.model

import enumeratum._

import scala.collection.immutable._

/**  Enum "encodable" representation of mark types available on the minefield.
  *
  *  @author david93111
  *
  *  Represent possible status of a game without the need of a FMS
  *  @see [[co.com.minesweeper.model.messages.GameState]]
  *
  *  Existing status are:
  *  Lose: state after a mine has been revealed
  *  Won: state after reveal all no Mine fields
  *  Active: Game just created or been played
  *
  *  Uses Enumeratum in order to achieve encoding of the Field class to
  *  be allow akka marshalling/un-marshalling on response or request
  *
  */
sealed trait GameStatus extends EnumEntry
case object GameStatus extends Enum[GameStatus] with CirceEnum[GameStatus] {

  case object Lose extends GameStatus
  case object Won extends GameStatus
  case object Active extends GameStatus

  val values: IndexedSeq[GameStatus] = findValues

}
