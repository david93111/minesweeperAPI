package co.com.minesweeper.api.handler

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{Directive0, MalformedRequestContentRejection, RejectionHandler, ValidationRejection}
import co.com.minesweeper.api.codecs.exception.ExceptionCodecs
import co.com.minesweeper.model.error.ServiceException
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
class CustomRejectionHandler(log: LoggingAdapter) extends ExceptionCodecs {

  val handler: RejectionHandler = RejectionHandler.newBuilder().handle({
    case ValidationRejection(msg, _) =>
      log.warning("Request Rejection: request does not meet the validations ")
      complete(BadRequest -> ServiceException("Invalid Parameters", msg))
    case MalformedRequestContentRejection(msg,_) =>
      log.warning("Request Rejection: request payload was invalid ")
      complete(BadRequest -> ServiceException("Invalid Payload", msg))
  }).result().withFallback(RejectionHandler.default)

  def rejectionHandler: Directive0 = handleRejections(handler)

}
