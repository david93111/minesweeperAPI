package co.com.minesweeper.api

import akka.http.scaladsl.model.DateTime
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.co.minesweeper.api.BuildInfo

trait ApiBase {

  def healthCheck(): Route = {
    (path("health_check") & get){
        complete(OK -> s"OK - ${DateTime.now.toString()}")
    }
  }

  def version(): Route = {
    (path("version") & get){
      complete(OK -> BuildInfo.version)
    }
  }

}
