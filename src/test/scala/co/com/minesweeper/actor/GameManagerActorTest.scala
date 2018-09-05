package co.com.minesweeper.actor

import akka.actor.{ActorRef, ActorSystem}
import akka.routing.FromConfig
import akka.testkit.{ImplicitSender, TestKit}
import co.com.minesweeper.BaseTest
import co.com.minesweeper.actor.GameManagerActor.{CreateGame, GetGame, SendMarkSpot, SendRevealSpot}
import co.com.minesweeper.model.error.GameOperationFailed
import co.com.minesweeper.model.{GameState, GameStatus, MarkType, MinefieldConfig}
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.ExecutionContextExecutor

class GameManagerActorTest extends TestKit(ActorSystem("gameManagerActor-system-test")) with ImplicitSender
  with BaseTest with BeforeAndAfterAll{

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Game Manager Actor Router Test Should" - {

    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val gameManagerActor: ActorRef = system.actorOf(FromConfig.props(GameManagerActor.props()), "gameManager")

    val minefieldConfig: MinefieldConfig = MinefieldConfig(2,3,1)

    "Create a game actor with specified parameters and Id on Manager Router" in {

      gameManagerActor ! CreateGame("game1", minefieldConfig, "user1")
      expectMsgPF()({
        case game: GameState =>
          game.gameId.shouldEqual("game1")
          game.minefield.rows shouldEqual 2
          game.minefield.columns shouldEqual 3
          game.minefield.mines shouldEqual 1
          game.gameStatus shouldEqual GameStatus.Active
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })

    }

    "Get a previously created Game from Router Manager with Hashing" in {

      gameManagerActor ! GetGame("game1")
      expectMsgPF()({
        case game: GameState =>
          game.gameId.shouldEqual("game1")
          game.minefield.rows shouldEqual 2
          game.minefield.columns shouldEqual 3
          game.minefield.mines shouldEqual 1
          game.gameStatus shouldEqual GameStatus.Active
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })

    }

    "Fail when trying to get a non existing game" in {

      gameManagerActor ! GetGame("nonExistingGame")
      expectMsgPF()({
        case game: GameOperationFailed =>
          game.gameId.shouldEqual("nonExistingGame")
          game.cause shouldEqual "GameOperationFailed"
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a GameOperationFailed")
      })

    }

    "Mark an existing field" in {

      gameManagerActor ! SendMarkSpot("game1",0,0, MarkType.QuestionMark)
      expectMsgPF()({
        case game: GameState =>
          game.gameId.shouldEqual("game1")
          game.minefield.board(0)(0).mark shouldEqual MarkType.QuestionMark
          game.gameStatus shouldEqual GameStatus.Active
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })

    }

    "Fail when trying to reveal an invalid cell" in {

      gameManagerActor ! SendRevealSpot("game1", -1, -1)
      expectMsgPF()({
        case game: GameOperationFailed =>
          game.gameId.shouldEqual("game1")
          game.cause shouldEqual "GameOperationFailed"
          game.message should startWith("Operation not allowed")
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a GameOperationFailed")
      })

    }

    "Reveal an existing unrevealed spot" in {

      gameManagerActor ! SendRevealSpot("game1",0,0)
      expectMsgPF()({
        case game: GameState =>
          game.gameId.shouldEqual("game1")
          game.minefield.board(0)(0).revealed shouldEqual true
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })

    }

  }

}
