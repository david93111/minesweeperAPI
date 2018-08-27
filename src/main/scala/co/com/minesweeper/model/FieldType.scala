package co.com.minesweeper.model

import enumeratum._

import scala.collection.immutable._

sealed trait FieldType extends EnumEntry
case object FieldType extends Enum[FieldType] with CirceEnum[FieldType] {

  case object Mine extends FieldType
  case object Empty extends FieldType
  case object Hint extends FieldType

  val values: IndexedSeq[FieldType] = findValues

}
