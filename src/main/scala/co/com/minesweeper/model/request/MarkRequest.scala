package co.com.minesweeper.model.request

import co.com.minesweeper.model.MarkType

case class MarkRequest(row: Int, col: Int, mark: MarkType){
  require(row >= 0, "The row must greater than zero")
  require(col >= 0, "The column must greater than zero")
}
