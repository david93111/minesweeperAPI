package co.com.minesweeper.model.request

case class RevealRequest(row: Int, col: Int){
  require(row >= 0, "The row must greater than zero")
  require(col >= 0, "The column must greater than zero")
}
