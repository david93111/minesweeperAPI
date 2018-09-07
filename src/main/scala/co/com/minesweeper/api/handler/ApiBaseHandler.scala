package co.com.minesweeper.api.handler

import akka.event.LoggingAdapter

/** Mixin for group all available handlers to be applied on the api akka route
  *
  * @author david93111
  *
  * */
trait ApiBaseHandler {

  val log: LoggingAdapter

  val corsHandler = new CorsHandler()
  val exceptionHandler = new CustomExceptionHandler(log)
  val rejectionHandler = new CustomRejectionHandler(log)

}
