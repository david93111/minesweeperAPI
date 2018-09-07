package co.com.minesweeper.model.request
import co.com.minesweeper.config.AppConf.{defaultColumns, defaultMines, defaultRows}

/** A request representation for create a new game .
  *
  *  @author david93111
  *  @constructor create a new request with given parameters.
  *  @param user number of row of field ro reveal
  *  @param rows Optional number of column of field ro reveal
  *  @param columns Optional number of column of field ro reveal
  *  @param mines Optional number of column of field ro reveal
  */
case class NewGameRequest (user: String, rows: Option[Int] = None, columns: Option[Int] = None, mines: Option[Int] = None)

/** Factory of GameSetting from a NewGameRequest .
  *
  *  @author david93111
  *  @constructor allow to create GameSetting from a NewGameRequest and execute validations on fields.
  *
  *  The only required parameter to create a game is the user, so the request can not be evaluated in
  *  a clean way because of the optionals, to avoid the required of all fields, a GameSetting is used
  *  to validate the optional fields if provided or use default valid parameters in contrary case
  */
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
