package co.com.minesweeper.api

import akka.actor.{ActorRef, ActorSystem}
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.routing.FromConfig
import co.com.minesweeper.actor.GameManagerActor
import co.com.minesweeper.api.services.{ApiServices, MinefieldServices}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.circe.syntax._
import co.com.minesweeper.config.AppConf.{defaultColumns, defaultMines, defaultRows}
import co.com.minesweeper.model.request.{MarkRequest, RevealRequest}

class Api()(implicit val actorSystem: ActorSystem) extends ApiBase with ApiServices{

  implicit val executionContext: MessageDispatcher = actorSystem.dispatchers.lookup("dispatchers.base-dispatcher")

  val gameManagerActor: ActorRef = actorSystem.actorOf(FromConfig.props(GameManagerActor.props()), "gameManager")

  val route: Route  = exceptionHandler.exceptionHandler{
    pathPrefix("minesweeper"){
      corsHandler.corsHandler {
        pathPrefix("game") {
          pathEndOrSingleSlash {
            post {
              parameter("user".as[String],"rows".as[Int] ? defaultRows, "columns".as[Int] ? defaultColumns, "mines".as[Int] ? defaultMines) {
                (username, rows, columns, mines) => {
                  createNewGame(rows, columns, mines, username)
                }
              }
            }~get{
              parameter("gameId".as[String]){ gameId =>
                getExistingGame(gameId)
              }
            }
          } ~ pathPrefix("reveal"){
            parameter("gameId".as[String]){ gameId =>
              put {
                entity(as[RevealRequest]) { reveal =>
                  revealSpot(gameId, reveal)
                }
              }
            }
          }~ pathPrefix("mark"){
            parameter("gameId".as[String]){ gameId =>
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
