package co.com.minesweeper.model.messages

import co.com.minesweeper.model.{GameStatus, Minefield}

/**  Representation of a game actor in a specific moment of time.
  *
  *  @author david93111
  *
  *  Contains all the required elements to validate the state of a game
  *  and also execute all the operations and validations needed inside the actor itself
  *
  *  This message is serialized for persistence using Akka-Kryo
  *
  *  @param gameId unique identifier of the actor and game in persistence
  *  @param gameStatus current game status at the time of the state was generated
  *  @param username owner of the game
  *  @param minefield minefield of game
  */
case class GameState(gameId: String,
                     gameStatus: GameStatus,
                     username: String,
                     minefield: Minefield,
                     lastAction: String = "Creation",
                     revealedCells: Int = 0,
                     timerInSeconds: Long = 0L,
                     paused: Boolean = false) extends GameResponseMessage
