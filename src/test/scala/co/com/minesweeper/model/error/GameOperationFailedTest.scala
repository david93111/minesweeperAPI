package co.com.minesweeper.model.error

import co.com.minesweeper.BaseTest

class GameOperationFailedTest extends BaseTest{

  "GameOperationFailed Test should" - {

    "Create a gameNotFound error with 404 status code" in{

      val failedGame = GameOperationFailed.gameNotFound("GameId")
      failedGame.statusCode shouldEqual 404
      failedGame.message should startWith("The requested game was not found")
    }

    "Create a cellAlreadyRevealed error with 409 status code" in{

      val failedGame = GameOperationFailed.cellAlreadyRevealed("GameId")
      failedGame.message shouldEqual "Cell can't be marked, is already revealed"
      failedGame.statusCode shouldEqual 409

    }

    "Create a GameFinishedOrPaused error with 409 status code" in{

      val failedGame = GameOperationFailed.GameFinishedOrPaused("GameId")
      failedGame.message should startWith("Operation not allowed, the game")
      failedGame.statusCode shouldEqual 409

    }

    "Create a GameFinished error with 409 status code" in{

      val failedGame = GameOperationFailed.GameFinished("GameId")
      failedGame.message should startWith("Operation not allowed")
      failedGame.statusCode shouldEqual 409

    }

    "Create a invalidCell error with 400 status code" in{

      val failedGame = GameOperationFailed.invalidCell("GameId")
      failedGame.message shouldEqual "Operation not allowed, selected mine is out of the game board range, rows"
      failedGame.statusCode shouldEqual 400

    }

  }

}
