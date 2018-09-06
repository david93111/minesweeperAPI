package co.com.minesweeper.api.handler

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import co.com.minesweeper.config.AppConf

class CorsHandler {

  val allowedOrigins: HttpOriginRange = {
    val origins = AppConf.allowedOrigins.map(HttpOrigin(_))
    if(origins.isEmpty) HttpOriginRange.* else HttpOriginRange(origins:_*)
  }

  //this directive adds access control headers to normal responses
  private def addAccessControlHeaders(): Directive0 = {
    respondWithHeaders(
      `Access-Control-Allow-Origin`(allowedOrigins),
      `Access-Control-Allow-Credentials`(true),
      `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With", "X-Auth-Token"),
      `Access-Control-Expose-Headers`("X-Auth-Token")
    )
  }

  //this handles preflight OPTIONS requests.
  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK).withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, PATCH)))
  }

  def corsHandler(route: Route): Route = addAccessControlHeaders() { route ~ preflightRequestHandler }

}
