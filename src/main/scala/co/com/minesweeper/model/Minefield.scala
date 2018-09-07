package co.com.minesweeper.model

/**  Representation of a Minefield.
  *
  *  @author david93111
  *
  *  Contains the board of the game composed by an array of Fields with two dimensions
  *
  *  Field can be a Mine, a Hint, or be Empty. Based on the existing field types
  *  @see [[co.com.minesweeper.model.FieldType]]
  *
  *  @param board array of two dimensions representing the board of the minesweeper
  *  @param rows direct access to number of rows of board to avoid iteration
  *  @param columns direct access to number of columns of board to avoid iteration
  *  @param mines direct access to number of mines of board to avoid iteration
  */
case class Minefield(board: Array[Array[Field]], rows: Int, columns: Int, mines: Int)
