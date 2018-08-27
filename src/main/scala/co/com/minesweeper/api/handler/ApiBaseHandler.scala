package co.com.minesweeper.api.handler

trait ApiBaseHandler {

  val corsHandler = new CorsHandler()
  val exceptionHandler = new CustomExceptionHandler()

}
