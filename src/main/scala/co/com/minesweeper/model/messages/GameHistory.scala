package co.com.minesweeper.model.messages

/**  Representation of a game state and movements history.
  *
  *  @author david93111
  *
  *  represented as a List of [[co.com.minesweeper.model.messages.GameState]]
  *
  *  This message is serialized for persistence using Akka-Kryo
  *
  *  @param historic current history of the game, Empty by default
  *
  */
case class GameHistory(historic: List[GameState] = Nil) extends GameResponseMessage {

  /**  Return new GameHistory with new state
    *  Add new provided state of the historic, and return a new representation of history
    */
  def update(move: GameState): GameHistory = {
    copy(move::historic)
  }

  /** Return current historic without accessing the property directly */
  def history: List[GameState] = historic

  override def toString: String = historic.reverse.mkString("\n")
}