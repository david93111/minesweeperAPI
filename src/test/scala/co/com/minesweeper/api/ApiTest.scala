package co.com.minesweeper.api

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import co.com.minesweeper.BaseTest
import co.com.minesweeper.api.codecs.Codecs
import co.com.minesweeper.model._
import co.com.minesweeper.model.request.NewGameRequest

import scala.concurrent.duration._

class ApiTest extends BaseTest with ScalatestRouteTest with Codecs{

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds.dilated)

  val log: LoggingAdapter = system.log
  val api: Api = new Api(log)(system)

  val route: Route = api.route

  "Api Route Test should" - {

    "Respond with 200 OK when health_check requested" in {

      Get("/minesweeper/health_check") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] should startWith("OK")
      }

    }

    "Respond with MethodNotAllowed when service requested whit inappropriate method" in {

      Put("/minesweeper/health_check") ~> route ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET, OPTIONS"
      }

    }

    "Respond with 200 OK for Game Creation service requested with default parameters" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      val gameRequest = NewGameRequest("user1")
      Post("/minesweeper/game", gameRequest) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = responseAs[Game]
        response.username shouldEqual "user1"
        response.gameStatus shouldEqual GameStatus.Active
      }

    }

    "Respond with 200 OK for Nested Get Game service request after Game Creation service request" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      val gameRequest = NewGameRequest("user1")
      Post("/minesweeper/game", gameRequest) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = responseAs[Game]
        Get(s"/minesweeper/game?gameId=${response.gameId}")~> route ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldEqual ContentTypes.`application/json`
          responseAs[Game].gameId shouldEqual response.gameId
        }
      }

    }

  }

}
