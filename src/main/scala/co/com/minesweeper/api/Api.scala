package co.com.minesweeper.api

import akka.actor.{ActorRef, ActorSystem}
import akka.dispatch.MessageDispatcher
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.routing.FromConfig
import co.com.minesweeper.actor.GameManagerActor
import co.com.minesweeper.api.services.ApiServices
import co.com.minesweeper.model.request.{MarkRequest, NewGameRequest, RevealRequest}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

class Api(val log: LoggingAdapter)(implicit val actorSystem: ActorSystem) extends ApiBase with ApiServices{

  implicit val executionContext: MessageDispatcher = actorSystem.dispatchers.lookup("dispatchers.base-dispatcher")

  val gameManagerActor: ActorRef = actorSystem.actorOf(FromConfig.props(GameManagerActor.props()), "gameManager")

  val route: Route  = exceptionHandler.exceptionHandler {
    rejectionHandler.rejectionHandler {
      pathPrefix("minesweeper") {
        corsHandler.corsHandler {
          pathPrefix("game") {
            pathEndOrSingleSlash {
              post {
                entity(as[NewGameRequest]) { gameRequest =>
                  createNewGame(NewGameRequest.GameSettingsFromGameRequest(gameRequest))
                }
              } ~ get {
                parameter("gameId".as[String]) { gameId =>
                  validate(gameId.nonEmpty, "The gameId must have a value") {
                    getExistingGame(gameId)
                  }
                }
              }
            } ~ pathPrefix("reveal") {
              parameter("gameId".as[String]) { gameId =>
                put {
                  entity(as[RevealRequest]) { reveal =>
                    revealSpot(gameId, reveal)
                  }
                }
              }
            } ~ pathPrefix("mark") {
              parameter("gameId".as[String]) { gameId =>
                put {
                  entity(as[MarkRequest]) { mark =>
                    markSpot(gameId, mark)
                  }
                }
              }
            }
          } ~ version() ~ healthCheck()
        }
      }
    }
  }

}
