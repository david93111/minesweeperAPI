package co.com.minesweeper.startup

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import co.com.minesweeper.api.Api
import co.com.minesweeper.config.AppConf.{appHost, appPort}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

/**  App entry point.
  *
  *  @author david93111
  *
  *  Starts Akka-HTTP server and actor system and bind it to the configured port and host
  *  Port and host can be changed on the application.conf at the http section or through JVM parameters
  *  In Case of failure the actor system is terminated
  *  On Success the register termination is setted up to stop server and actor system on shutdown hook
  *
  *  Creates a instance of the Api class that contains the akka route definition, codecs and handlers
  *  @see [[co.com.minesweeper.api.Api]]
  */
object Boot extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("minesweeper-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  val log: LoggingAdapter = Logging(actorSystem.eventStream, "akka_minesweeper_log")

  val api = new Api(log)(actorSystem, materializer)
  val bindingFuture = Http().bindAndHandle(api.route, appHost , appPort)

  bindingFuture.onComplete{
    case Success(value) =>
      log.info(s"Binding Successfully achieved, server started on $appHost:$appPort")
      actorSystem.registerOnTermination(value.unbind())
    case Failure(exception) =>
      log.error(s"HTTP server binding Failed, starting system shutdown, cause: $exception")
      actorSystem.terminate()
  }

}
