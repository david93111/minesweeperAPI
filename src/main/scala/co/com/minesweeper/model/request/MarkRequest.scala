package co.com.minesweeper.model.request

import co.com.minesweeper.model.MarkType

/** A request representation for revealing a field .
  *
  *  @author david93111
  *  @constructor create a new request with given parameters.
  *  @param row number of row of field ro mark
  *  @param col number of column of field ro mark
  *  @param mark mark type to be applied on the field
  *
  *  @see [[co.com.minesweeper.model.MarkType]]
  */
case class MarkRequest(row: Int, col: Int, mark: MarkType){
  require(row >= 0, "The row must greater than zero")
  require(col >= 0, "The column must greater than zero")
}
