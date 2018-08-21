package co.com.minesweeper.api

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import co.com.minesweeper.api.codecs.Codecs
import co.com.minesweeper.api.services.MinefieldServices
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

class Api()(implicit val executionContext: ExecutionContext) extends ApiBase with Codecs{

  val route: Route  = pathPrefix("minesweeper"){
    pathPrefix("minefield"){
      get{
        complete(OK -> MinefieldServices.createMinefield())
      }
    } ~ version() ~ healthCheck()
  }

}
