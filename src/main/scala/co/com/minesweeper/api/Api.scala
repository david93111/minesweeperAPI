package co.com.minesweeper.api

import akka.actor.{ActorRef, ActorSystem}
import akka.dispatch.MessageDispatcher
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.routing.FromConfig
import akka.stream.ActorMaterializer
import co.com.minesweeper.actor.GameManagerActor
import co.com.minesweeper.api.services.ApiServices
import co.com.minesweeper.model.request.{MarkRequest, NewGameRequest, RevealRequest}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

/**  RestFul Api Route for Game Services
  *
  *  @author david93111
  *
  *  Definition of Akka-HTTP route with all the services exposed for playing the minesweeper game
  *
  *  Expose service of game creation, recover a game and reveal or mark fields in a given game by id
  *  Also expose services for pause or resume a game or see gamy history
  *  Expose service of version to be able to see deployment version and verify if compatible with client library
  *
  *  Uses ActorSystem to create a parametrized Actor Router with Hashing for guarantee game state, also compatible
  *  With cluster behavior to be implemented in the future using Shardinng
  *
  *  The ApiServices layer on [[co.com.minesweeper.api.services.ApiServices]] is on charge of sending messages to Router
  *
  *  Exceptions and Rejections are managed using default custom Handlers provided by [[co.com.minesweeper.api.ApiBase]]
  *
  * */
class Api(val log: LoggingAdapter)(implicit val actorSystem: ActorSystem, materializer: ActorMaterializer) extends ApiBase with ApiServices{

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
            } ~ path(Segment / "reveal") { gameId =>
                put {
                  entity(as[RevealRequest]) { reveal =>
                    revealSpot(gameId, reveal)
                  }
                }
            } ~ path(Segment / "mark") { gameId =>
                put {
                  entity(as[MarkRequest]) { mark =>
                    markSpot(gameId, mark)
                  }
                }
            } ~ path(Segment / "pause") { gameId =>
              patch {
                pauseGame(gameId)
              }
            } ~ path(Segment / "resume") { gameId =>
              patch {
                resumeGame(gameId)
              }
            } ~ path(Segment / "history") { gameId =>
              get {
                getGameHistory(gameId)
              }
            }
          } ~ version() ~ healthCheck()
        }
      }
    }
  }

}
