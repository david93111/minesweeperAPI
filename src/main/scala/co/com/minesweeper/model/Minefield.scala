package co.com.minesweeper.model

case class Minefield(board: Array[Array[Field]], rows: Int, columns: Int, mines: Int)
