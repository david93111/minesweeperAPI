package co.com.minesweeper.model

import enumeratum.CirceEnum
import enumeratum._
import scala.collection.immutable._

sealed trait MarkType extends EnumEntry
case object MarkType extends Enum[MarkType] with CirceEnum[MarkType] {

  case object QuestionMark extends MarkType
  case object FlagMark extends MarkType
  case object None extends MarkType

  val values: IndexedSeq[MarkType] = findValues

}
