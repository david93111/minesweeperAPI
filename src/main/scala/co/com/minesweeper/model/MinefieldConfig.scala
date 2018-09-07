package co.com.minesweeper.model

/**  Configuration of minefield for a new game.
  *
  *  @author david93111
  *
  *  @param rows number of max rows of the minefield
  *  @param columns number of max columns of the minefield
  *  @param mines number of total mines to be added to the minefield
  */
case class MinefieldConfig(rows: Int, columns: Int, mines: Int)
