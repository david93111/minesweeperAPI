package co.com.minesweeper.api.handler

import akka.event.LoggingAdapter

trait ApiBaseHandler {

  val log: LoggingAdapter

  val corsHandler = new CorsHandler()
  val exceptionHandler = new CustomExceptionHandler(log)
  val rejectionHandler = new CustomRejectionHandler(log)

}
