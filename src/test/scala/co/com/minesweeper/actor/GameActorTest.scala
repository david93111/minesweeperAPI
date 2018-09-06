package co.com.minesweeper.actor

import akka.actor.{ActorSystem, Props}
import akka.persistence.{Recovery, SnapshotSelectionCriteria}
import akka.persistence.inmemory.extension.{InMemoryJournalStorage, InMemorySnapshotStorage, StorageExtension}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout
import co.com.minesweeper.BaseTest
import co.com.minesweeper.actor.GameActor.{GetMinefield, MarkSpot, RevealSpot}
import co.com.minesweeper.api.services.MinefieldService
import co.com.minesweeper.model.GameStatus.Won
import co.com.minesweeper.model.error.GameOperationFailed
import co.com.minesweeper.model._
import co.com.minesweeper.model.messages.GameState
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration.{FiniteDuration, SECONDS}

class GameActorTest extends TestKit(ActorSystem("gameActor-system-test")) with ImplicitSender
  with BaseTest with BeforeAndAfterAll {

  implicit val timeout: Timeout = Timeout(FiniteDuration(10L, SECONDS))

  override def beforeAll: Unit = {
    val tp = TestProbe()
    tp.send(StorageExtension(system).journalStorage, InMemoryJournalStorage.ClearJournal)
    tp.expectMsg(akka.actor.Status.Success(""))
    tp.send(StorageExtension(system).snapshotStorage, InMemorySnapshotStorage.ClearSnapshots)
    tp.expectMsg(akka.actor.Status.Success(""))
    super.beforeAll()
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val conf = MinefieldConfig(6, 5, 0)
  val user = "user1"

  "GameActor Test Should create a new game actor and then should" - {
    val gameState = GameState("game1", GameStatus.Active, user, MinefieldService.createMinefield(conf.columns, conf.rows, conf.mines))
    val gameActorProps = Props(
      new GameActor("game1", gameState){
        override def recovery = Recovery(fromSnapshot = SnapshotSelectionCriteria.None, replayMax = 0L)
      }
    )
    val gameActor = system.actorOf(GameActor.props("game1" , gameState))

    "Get game expected from actor through message" in {
      gameActor ! GetMinefield
      expectMsgPF()({
        case game: GameState =>
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
        case game: GameState =>
          game.gameId.shouldEqual("game1")
          game.minefield.board(0)(1).mark shouldEqual MarkType.FlagMark
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })
    }

    "Send message to actor to mark a spot with a Question" in {
      gameActor ! MarkSpot(0,2, MarkType.QuestionMark)
      expectMsgPF()({
        case game: GameState =>
          game.gameId.shouldEqual("game1")
          game.minefield.board(0)(2).mark shouldEqual MarkType.QuestionMark
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })
    }

    "Remove an existing mark on a spot marked and not revealed" in {
      gameActor ! MarkSpot(0,1, MarkType.None)
      expectMsgPF()({
        case game: GameState =>
          game.gameId.shouldEqual("game1")
          game.minefield.board(0)(0).mark shouldEqual MarkType.None
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })
    }

    "Allow to remove a mark even on a revealed spot if game active" in {
      gameActor ! MarkSpot(0,0, MarkType.None)
      expectMsgPF()({
        case game: GameState =>
          game.gameStatus shouldEqual GameStatus.Active
          game.minefield.board(0)(0).mark shouldEqual MarkType.None
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })
    }

    "Send message to actor to reveal a spot" in {
      gameActor ! RevealSpot(0,0)
      expectMsgPF()({
        case game: GameState =>
          game.gameId.shouldEqual("game1")
          game.minefield.board(0)(0).revealed shouldEqual true
          game.gameStatus shouldEqual Won
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })
    }

    "Fail after send message to actor to mark a spot on a finished game" in {
      gameActor ! MarkSpot(0,0, MarkType.QuestionMark)
      expectMsgPF()({
        case error: GameOperationFailed =>
          error.statusCode shouldEqual 409
          error.message should startWith("Operation not allowed")
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a GameOperationFailed")
      })
    }

    "Fail after send message to actor to reveal a spot on a finished game" in {
      gameActor ! RevealSpot(0,1)
      expectMsgPF()({
        case error: GameOperationFailed =>
          error.statusCode shouldEqual 409
          error.message should startWith("Operation not allowed")
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a GameOperationFailed")
      })
    }

    "Create new game and reveal a mine, game must pass to Lose state" in {
      val config = MinefieldConfig(2, 2, 4)
      val game2 = GameState("game2", GameStatus.Active, "user2", MinefieldService.createMinefield(config.columns, config.rows, config.mines))
      val game = system.actorOf(GameActor.props("game2",  game2))
      game ! RevealSpot(0,0)
      expectMsgPF()({
        case game: GameState =>
          game.gameId.shouldEqual("game2")
          game.minefield.board(0)(0).revealed shouldEqual true
          game.gameStatus shouldEqual GameStatus.Lose
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })
    }

    "Create new game , get game board and then reveal a Hint, game must remain active" in {
      val config = MinefieldConfig(2, 2, 1)
      val game3 = GameState("game3", GameStatus.Active, "user2", MinefieldService.createMinefield(config.columns, config.rows, config.mines))
      val actor = system.actorOf(GameActor.props("game3" , game3))
      actor ! GetMinefield
      expectMsgPF()({
        case game: GameState =>
          game.gameId.shouldEqual("game3")
          game.gameStatus shouldEqual GameStatus.Active
          val targetHint = game.minefield.board.map(_.toList).toList.flatten.filter(_.fieldType == FieldType.Hint).head
          actor ! RevealSpot(targetHint.row, targetHint.col)
          expectMsgPF()({
            case game: GameState =>
              game.gameId.shouldEqual("game3")
              game.minefield.board(targetHint.row)(targetHint.col).revealed shouldEqual true
              game.gameStatus shouldEqual GameStatus.Active
            case e =>
              fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
          })
        case e =>
          fail(s"Message type of type ${e.getClass.getSimpleName} expected to be a Game")
      })

    }

  }

}
