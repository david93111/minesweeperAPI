package co.com.minesweeper.model

import enumeratum._

import scala.collection.immutable._

sealed trait GameStatus extends EnumEntry
case object GameStatus extends Enum[GameStatus] with CirceEnum[GameStatus] {

  case object Lose extends GameStatus
  case object Won extends GameStatus
  case object Active extends GameStatus

  val values: IndexedSeq[GameStatus] = findValues

}
