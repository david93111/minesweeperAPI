package co.com.minesweeper.actor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import co.com.minesweeper.BaseTest
import co.com.minesweeper.actor.GameActor.{GetMinefield, MarkSpot, RevealSpot}
import co.com.minesweeper.model.error.GameOperationFailed
import co.com.minesweeper.model.{Game, GameStatus, MarkType, MinefieldConfig}
import org.scalatest.BeforeAndAfterAll

class GameActorTest extends TestKit(ActorSystem("minesweeper-system-test")) with ImplicitSender
  with BaseTest with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val minefieldConfig = MinefieldConfig(6, 5, 0)
  val user = "user1"

  "GameActor Test Should create a new game actor and then should" - {

    val gameActor = system.actorOf(GameActor.props("game1" ,minefieldConfig, user))

    "Get game expected from actor through message" in {
      gameActor ! GetMinefield
      expectMsgPF()({
        case game: Game =>
          game.gameId.shouldEqual("game1")
          game.minefield.rows shouldEqual 6
          game.minefield.columns shouldEqual 5
          game.gameStatus shouldEqual GameStatus.Active
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })
    }

    "Send message to actor to mark a spot with a flag" in {
      gameActor ! MarkSpot(0,1, MarkType.FlagMark)
      expectMsgPF()({
        case game: Game =>
          game.gameId.shouldEqual("game1")
          game.minefield.board(0)(1).mark shouldEqual MarkType.FlagMark
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })
    }

    "Send message to actor to mark a spot with a Question" in {
      gameActor ! MarkSpot(0,2, MarkType.QuestionMark)
      expectMsgPF()({
        case game: Game =>
          game.gameId.shouldEqual("game1")
          game.minefield.board(0)(2).mark shouldEqual MarkType.QuestionMark
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })
    }

    "Send message to actor mark spot as No marked" in {
      gameActor ! MarkSpot(0,1, MarkType.None)
      expectMsgPF()({
        case game: Game =>
          game.gameId.shouldEqual("game1")
          game.minefield.board(0)(0).mark shouldEqual MarkType.None
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })
    }

    "Not Fail after send message to actor to remove mark on a revealed spot" in {
      gameActor ! MarkSpot(0,0, MarkType.None)
      expectMsgPF()({
        case game: Game =>
          game.gameStatus shouldEqual GameStatus.Active
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })
    }

    "Send message to actor to reveal a spot" in {
      gameActor ! RevealSpot(0,0)
      expectMsgPF()({
        case game: Game =>
          game.gameId.shouldEqual("game1")
          game.minefield.board(0)(0).revealed shouldEqual true
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })
    }

    "Fail after send message to actor to mark a spot on a finished game" in {
      gameActor ! MarkSpot(0,0, MarkType.QuestionMark)
      expectMsgPF()({
        case error: GameOperationFailed =>
          error.message shouldEqual "Operation not allowed, the game has finished already"
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a GameOperationFailed")
      })
    }

    "Fail after send message to actor to reveal a spot on a finished game" in {
      gameActor ! RevealSpot(0,1)
      expectMsgPF()({
        case error: GameOperationFailed =>
          error.message shouldEqual "Operation not allowed, the game has finished already"
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a GameOperationFailed")
      })
    }

  }

}
