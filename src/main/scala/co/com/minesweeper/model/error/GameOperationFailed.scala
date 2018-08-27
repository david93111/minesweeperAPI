package co.com.minesweeper.model.error

import co.com.minesweeper.model.messages.GameResponseMessage

case class GameOperationFailed(message: String, gameId: String, statusCode: Int) extends GameResponseMessage

object GameOperationFailed{

  def gameNotFound(id: String): GameOperationFailed = {
    new GameOperationFailed("The requested game was not found on memory or does not exist, please create a new one", id, 404)
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