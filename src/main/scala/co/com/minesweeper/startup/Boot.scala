package co.com.minesweeper.startup

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import co.com.minesweeper.api.Api
import co.com.minesweeper.config.AppConf.{appHost, appPort}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Boot extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("minesweeper-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  val log: LoggingAdapter = Logging(actorSystem.eventStream, "akka_minesweeper")

  val api = new Api(log)(actorSystem)
  val bindingFuture = Http().bindAndHandle(api.route, appHost , appPort)

  bindingFuture.onComplete{
    case Success(value) =>
      log.info("Binding Successfully achieved, server started")
      actorSystem.registerOnTermination(value.unbind())
    case Failure(exception) =>
      log.error(s"HTTP server binding Failed, starting system shutdown, cause: $exception")
      actorSystem.terminate()
  }

}
