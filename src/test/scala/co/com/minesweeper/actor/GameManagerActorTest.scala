package co.com.minesweeper.actor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.contrib.persistence.mongodb.ScalaDslMongoReadJournal
import akka.persistence.inmemory.extension.{InMemoryJournalStorage, InMemorySnapshotStorage, StorageExtension}
import akka.routing.FromConfig
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout
import co.com.minesweeper.BaseTest
import co.com.minesweeper.actor.GameManagerActor.{CreateGame, GetGame, SendMarkSpot, SendRevealSpot}
import co.com.minesweeper.model.error.GameOperationFailed
import co.com.minesweeper.model.messages.GameState
import co.com.minesweeper.model.{GameStatus, MarkType, MinefieldConfig}
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{FiniteDuration, SECONDS}

class GameManagerActorTest extends TestKit(ActorSystem("GameManagerActorTest-system")) with ImplicitSender
  with BaseTest with BeforeAndAfterAll{

  implicit val timeout: Timeout = Timeout(FiniteDuration(10L, SECONDS))

  // Clear in journal memory for default behavior on actor persistence
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

  "Game Manager Actor Router Test Should" - {

    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val actorManagerOverrideJournal = Props(new GameManagerActor()(executionContext,materializer){
      override def loadJournal: Option[ScalaDslMongoReadJournal] = None
    })

    val gameManagerActor: ActorRef = system.actorOf(FromConfig.props(actorManagerOverrideJournal), "gameManager")

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
