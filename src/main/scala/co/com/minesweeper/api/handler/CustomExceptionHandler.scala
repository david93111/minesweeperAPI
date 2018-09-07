package co.com.minesweeper.api.handler

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives.{complete, handleExceptions}
import akka.http.scaladsl.server.{Directive0, ExceptionHandler}
import co.com.minesweeper.api.codecs.exception.ExceptionCodecs
import co.com.minesweeper.model.error.ServiceException
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

/** Custom exception handler for unexpected exception between api and api services layer */
class CustomExceptionHandler(log: LoggingAdapter) extends ExceptionCodecs{

  val handler: ExceptionHandler = ExceptionHandler {
    case illegalArgument: java.lang.IllegalArgumentException =>
      log.error("Invalid request made {}", illegalArgument)
      complete(BadRequest -> ServiceException("Invalid Parameters", illegalArgument.getMessage))
    case e: Throwable =>
      log.error("Unexpected error, cause: {}", e)
      complete(InternalServerError -> ServiceException("Unexpected Server Error", s"Error Cause: ${e.getMessage}"))
  }

  def exceptionHandler: Directive0 = handleExceptions(handler)

}
