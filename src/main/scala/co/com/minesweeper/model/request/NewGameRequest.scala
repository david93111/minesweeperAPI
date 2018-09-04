package co.com.minesweeper.model.request
import co.com.minesweeper.config.AppConf.{defaultColumns, defaultMines, defaultRows}

case class NewGameRequest (user: String, rows: Option[Int] = None, columns: Option[Int] = None, mines: Option[Int] = None)

object NewGameRequest{

  case class GameSettings(user: String, rows: Int, columns: Int, mines: Int) {
    require(user.trim.nonEmpty && !user.equals("null"), "User can not be empty or string literal null")
    require(rows > 0 && rows <= 30, "Rows of the minefield must be greater than zero")
    require(columns > 0 && columns <= 30, "Columns of the minefield must be greater than zero")
    require( mines > 0 , "Mines to be placed must be greater than zero")
  }

  def GameSettingsFromGameRequest(gameRequest: NewGameRequest): GameSettings = {
      GameSettings(
        gameRequest.user,
        gameRequest.rows.getOrElse(defaultRows),
        gameRequest.columns.getOrElse(defaultColumns),
        gameRequest.mines.getOrElse(defaultMines)
      )
  }
}
