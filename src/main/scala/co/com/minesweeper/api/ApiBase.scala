package co.com.minesweeper.api

import akka.http.scaladsl.model.DateTime
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import co.com.minesweeper.api.handler.ApiBaseHandler
import com.co.minesweeper.api.BuildInfo


/**  Api technical services
  *
  *  @author david93111
  *
  *  includes handlers for Rejections and Exceptions, also management of Cross Site
  *
  *  Expose service of health check for deployment and balancing
  *  Expose service of version to be able to see deployment version and verify if compatible with client library
  *
  * */
trait ApiBase extends ApiBaseHandler{

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
