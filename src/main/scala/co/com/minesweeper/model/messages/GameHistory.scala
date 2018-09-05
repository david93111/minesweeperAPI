package co.com.minesweeper.model.messages

case class GameHistory(historic: List[GameState] = Nil) extends GameResponseMessage {
  def update(move: GameState): GameHistory = {
    copy(move::historic)
  }
  def getLastMovement: Option[GameState] = historic.headOption

  override def toString: String = historic.reverse.mkString("\n")
}