package co.com.minesweeper.startup

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import co.com.minesweeper.api.Api
import co.com.minesweeper.config.AppConf.appConfig

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Boot extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  val defaultDispatcher: MessageDispatcher = actorSystem.dispatchers.lookup("dispatchers.base-dispatcher")

  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  val api = new Api()(defaultDispatcher)

  val port = appConfig.getInt("http.port")
  val host = appConfig.getString("http.host")

  val bindingFuture = Http().bindAndHandle(api.route, host , port)

  bindingFuture.onComplete{
    case Success(value) =>
      println("Binding Successfully achieved")
      actorSystem.registerOnTermination(value.unbind())
    case Failure(exception) =>
      println("Binding Failed")
      actorSystem.terminate()
  }

}
