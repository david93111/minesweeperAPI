package co.com.minesweeper.model

import co.com.minesweeper.model.messages.GameResponseMessage

case class Game(gameId: String, gameStatus: GameStatus, username: String, minefield: Minefield) extends GameResponseMessage
