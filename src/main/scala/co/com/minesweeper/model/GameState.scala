package co.com.minesweeper.model

import co.com.minesweeper.model.messages.GameResponseMessage

case class GameState(gameId: String, gameStatus: GameStatus, username: String, minefield: Minefield,
                     lastAction:String="Creation", revealedCells: Int = 0, timerInSeconds: Long = 0L, paused: Boolean) extends GameResponseMessage
