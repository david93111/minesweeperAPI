package co.com.minesweeper.model.error

import co.com.minesweeper.model.messages.GameResponseMessage

case class GameOperationFailed(message: String, gameId: String, statusCode: Int, cause: String = "GameOperationFailed") extends GameResponseMessage

object GameOperationFailed{

  def gameNotFound(id: String, messsage: String = ""): GameOperationFailed = {
    new GameOperationFailed(s"The requested game was not found: $messsage", id, 404)
  }

  def invalidCell(id: String): GameOperationFailed = {
    new GameOperationFailed("Operation not allowed, selected mine is out of the game board range, rows", id, 400)
  }

  def cellAlreadyRevealed(id: String): GameOperationFailed = {
    new GameOperationFailed("Cell can't be marked, is already revealed", id, 409)
  }

  def GameFinished(id: String): GameOperationFailed = {
    new GameOperationFailed("Operation not allowed, the game has finished already", id, 409)
  }

}