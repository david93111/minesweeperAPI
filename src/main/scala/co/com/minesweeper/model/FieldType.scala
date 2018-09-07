package co.com.minesweeper.model

import enumeratum._

import scala.collection.immutable._

/**  Enum "encodable" representation of minefield box types.
  *
  *  @author david93111
  *
  *  Represent possible status of a game without the need of a FMS
  *  @see [[co.com.minesweeper.model.messages.GameState]]
  *
  *  Existing field types are:
  *  Mine: Field that contains a mine, if revealed game is lost
  *  Empty: Field near to other empty fields or hints
  *  Hint: Field with near mines
  *
  *  Uses Enumeratum in order to achieve encoding of the Field class to
  *  be allow akka marshalling/un-marshalling on response or request
  *
  */
sealed trait FieldType extends EnumEntry
case object FieldType extends Enum[FieldType] with CirceEnum[FieldType] {

  case object Mine extends FieldType
  case object Empty extends FieldType
  case object Hint extends FieldType

  val values: IndexedSeq[FieldType] = findValues

}
