package co.com.minesweeper.model.request

/** A request representation for revealing a field .
  *
  *  @author david93111
  *  @constructor create a new request with given parameters.
  *  @param row number of row of field ro reveal
  *  @param col number of column of field ro reveal
  */
case class RevealRequest(row: Int, col: Int){
  require(row >= 0, "The row must greater than zero")
  require(col >= 0, "The column must greater than zero")
}
