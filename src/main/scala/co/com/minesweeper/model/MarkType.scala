package co.com.minesweeper.model

import enumeratum.CirceEnum
import enumeratum._
import scala.collection.immutable._

/**  Enum "encodable" representation of mark types available on the minefield.
  *
  *  @author david93111
  *
  *  Represent possible logical marks to be applied on Field
  *  @see [[co.com.minesweeper.model.Field]]
  *
  *  Existing types are:
  *  QuestionMark: Represent a question symbol putted on the field
  *  FlagMark: Represent a red flag symbol putted on the field
  *  None: Represent logical absence of mark
  *
  *  Uses Enumeratum in order to achieve encoding of the Field class to
  *  be allow akka marshalling/un-marshalling on response or request
  *
  */
sealed trait MarkType extends EnumEntry
case object MarkType extends Enum[MarkType] with CirceEnum[MarkType] {

  case object QuestionMark extends MarkType
  case object FlagMark extends MarkType
  case object None extends MarkType

  val values: IndexedSeq[MarkType] = findValues

}
