package co.com.minesweeper.api.handler

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{ complete, handleExceptions }
import akka.http.scaladsl.server.{ Directive0, ExceptionHandler }

class CustomExceptionHandler {

  val handler: ExceptionHandler = ExceptionHandler {
    case e: Exception =>
      complete(StatusCodes.InternalServerError -> s"Unexpected error trying to fulfill the operation, Cause: $e")
  }

  def exceptionHandler: Directive0 = handleExceptions(handler)

}
