package co.com.minesweeper.api

import akka.actor.{ActorRef, Props}
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.persistence.inmemory.extension.{InMemoryJournalStorage, InMemorySnapshotStorage, StorageExtension}
import akka.routing.FromConfig
import akka.testkit.{TestDuration, TestProbe}
import co.com.minesweeper.BaseTest
import co.com.minesweeper.actor.GameManagerActor
import co.com.minesweeper.actor.GameManagerActor.GameManagerReadJournal
import co.com.minesweeper.api.codecs.Codecs
import co.com.minesweeper.model._
import co.com.minesweeper.model.error.{GameOperationFailed, ServiceException}
import co.com.minesweeper.model.messages.{GameHistory, GameState}
import co.com.minesweeper.model.request.{MarkRequest, NewGameRequest, RevealRequest}
import com.co.minesweeper.api.BuildInfo

import scala.concurrent.duration._

class ApiTest extends BaseTest with ScalatestRouteTest with Codecs{

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds.dilated)

  override def beforeAll: Unit = {
    val tp = TestProbe()
    tp.send(StorageExtension(system).journalStorage, InMemoryJournalStorage.ClearJournal)
    tp.expectMsg(akka.actor.Status.Success(""))
    tp.send(StorageExtension(system).snapshotStorage, InMemorySnapshotStorage.ClearSnapshots)
    tp.expectMsg(akka.actor.Status.Success(""))
    super.beforeAll()
  }

  val log: LoggingAdapter = system.log
  val api: Api = new Api(log)(system,materializer){
    val actorManagerOverrideJournal = Props(new GameManagerActor()(executionContext,materializer){
      override def loadJournal: Option[GameManagerReadJournal] = None
    })
    override val gameManagerActor: ActorRef = actorSystem.actorOf(FromConfig.props(actorManagerOverrideJournal), "gameManagerTest")
  }

  val route: Route = api.route

  "Api Route Test should" - {

    var gameId : String =  ""

    "Respond with 200 OK when health_check requested" in {

      Get("/minesweeper/health_check") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] should startWith("OK")
      }

    }

    "Respond with 200 OK when version requested, version must match " in {

      Get("/minesweeper/version") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual BuildInfo.version
      }

    }

    "Respond with 405 MethodNotAllowed when a service is requested with inappropriate method" in {

      Put("/minesweeper/health_check") ~> route ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET, OPTIONS"
      }

    }

    "Respond with 405 MethodNotAllowed when a non existing service is consumed" in {

      Get("/minesweeper/notExistingMethod") ~> route ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: OPTIONS"
      }

    }

    "Respond with 200 OK for Game Creation service requested with default parameters" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      val gameRequest = NewGameRequest("user1")
      Post("/minesweeper/game", gameRequest) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = responseAs[GameState]
        response.username shouldEqual "user1"
        response.gameStatus shouldEqual GameStatus.Active
        gameId = response.gameId
      }
    }

    "Respond with 200 OK for Get Game service request after previous Game Creation " in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      Get(s"/minesweeper/game?gameId=$gameId") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        responseAs[GameState].gameId shouldEqual gameId
      }
    }

    "Respond with 400 Bad Request for Game Creation service with invalid parameters" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      val gameRequest = NewGameRequest("badUser", Some(2), Some(2), Some(0))
      Post("/minesweeper/game", gameRequest) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`application/json`
        val response = responseAs[ServiceException]
        response.cause shouldEqual "Invalid Parameters"
      }
    }

    "Respond with 400 Bad Request for Game Creation service with invalid payload" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      Post("/minesweeper/game", "InvalidPayload") ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual ContentTypes.`application/json`
        val response = responseAs[ServiceException]
        response.cause shouldEqual "Invalid Payload"
      }
    }

    "Respond with 200 OK for mark a spot with question symbol" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      val markRequest = MarkRequest(0, 0, MarkType.QuestionMark )
      Put(s"/minesweeper/game/$gameId/mark", markRequest) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = responseAs[GameState]
        response.minefield.board(0)(0).mark shouldEqual MarkType.QuestionMark
      }
    }

    "Respond with 200 OK for mark a spot with a flag" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      val markRequest = MarkRequest(0, 1, MarkType.FlagMark )
      Put(s"/minesweeper/game/$gameId/mark", markRequest) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = responseAs[GameState]
        response.minefield.board(0)(1).mark shouldEqual MarkType.FlagMark
      }
    }

    "Respond with 200 OK for remove a mark on a marked spot" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      val markRequest = MarkRequest(0, 0, MarkType.None )
      Put(s"/minesweeper/game/$gameId/mark", markRequest) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = responseAs[GameState]
        response.minefield.board(0)(0).mark shouldEqual MarkType.None
      }
    }

    "Respond with 200 OK when pause a game" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      Patch(s"/minesweeper/game/$gameId/pause") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = responseAs[GameState]
        response.paused shouldEqual true
      }
    }

    "Respond with 200 OK when resume a game" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      Patch(s"/minesweeper/game/$gameId/resume") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = responseAs[GameState]
        response.paused shouldEqual false
      }
    }

    "Respond with 200 OK with all movements of a game when history requested" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      Get(s"/minesweeper/game/$gameId/history") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = responseAs[GameHistory]
        response.history.size should be > 0
      }
    }

    "Respond with 200 OK when reveal a spot" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      val revealRequest = RevealRequest(0, 0)
      Put(s"/minesweeper/game/$gameId/reveal", revealRequest) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = responseAs[GameState]
        response.minefield.board(0)(0).revealed shouldEqual true
      }
    }

    "Respond with 404 Not Found when game does not exist" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      Get(s"/minesweeper/game?gameId=NonExistingGame") ~> route ~> check {
        status shouldEqual StatusCodes.NotFound
        contentType shouldEqual ContentTypes.`application/json`
        val result = responseAs[GameOperationFailed]
        result.gameId shouldEqual "NonExistingGame"
        result.cause shouldEqual "GameOperationFailed"
      }
    }

    "Respond with 200 OK for request of available options" in {
      Options(s"/minesweeper") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        header("Access-Control-Allow-Methods").isDefined shouldEqual true
      }
    }

  }

}
