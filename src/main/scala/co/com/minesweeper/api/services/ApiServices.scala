package co.com.minesweeper.api.services

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import co.com.minesweeper.actor.GameActor
import co.com.minesweeper.actor.GameManagerActor._
import co.com.minesweeper.api.codecs.Codecs
import co.com.minesweeper.model.error.{GameOperationFailed, ServiceException}
import co.com.minesweeper.model.messages.{GameBasicOperation, GameHistory, GameResponseMessage, GameState}
import co.com.minesweeper.model.request.NewGameRequest.GameSettings
import co.com.minesweeper.model.request.{MarkRequest, RevealRequest}
import co.com.minesweeper.model.MinefieldConfig
import co.com.minesweeper.util.BaseTimeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success}

trait ApiServices extends Codecs with BaseTimeout{

  val actorSystem: ActorSystem

  implicit val executionContext: MessageDispatcher

  val gameManagerActor: ActorRef

  def createNewGame(settings: GameSettings): Route ={
    val gameId = UUID.randomUUID().toString
    val gameMessage = CreateGame(gameId, MinefieldConfig(settings.rows, settings.columns, settings.mines), settings.user)
    val gameCreation = gameManagerActor ? gameMessage
    val result = gameCreation.mapTo[GameState]
    onComplete(result){
      case Failure(exception) =>
        complete(InternalServerError -> ServiceException("GameCreationError",s"Unexpected error creating new game cause: ${exception.getMessage}"))
      case Success(value) =>
          complete(OK -> value)
    }
  }

  def getExistingGame(gameId: String): Route = {
    val getGame = gameManagerActor ? GetGame(gameId)
    resolveGameResponseFuture(getGame.mapTo[GameResponseMessage])
  }

  def revealSpot(gameId: String, revealRequest: RevealRequest): Route = {
    val revealAction = gameManagerActor ? SendRevealSpot(gameId, revealRequest.row, revealRequest.col)
    resolveGameResponseFuture(revealAction.mapTo[GameResponseMessage])
  }

  def markSpot(gameId: String, markRequest: MarkRequest): Route = {
    val markAction = gameManagerActor ? SendMarkSpot(gameId, markRequest.row, markRequest.col, markRequest.mark)
    resolveGameResponseFuture(markAction.mapTo[GameResponseMessage])
  }

  def pauseGame(gameId: String): Route = {
    sendBasicOperationMessage(gameId, GameActor.PauseGame)
  }

  def resumeGame(gameId: String): Route = {
    sendBasicOperationMessage(gameId, GameActor.ResumeGame)
  }

  def getGameHistory(gameId: String): Route = {
    sendBasicOperationMessage(gameId, GameActor.GetHistory)
  }

  def sendBasicOperationMessage(gameId: String, message: GameBasicOperation): Route = {
    val askMessage = gameManagerActor ? SendGameMessage(gameId, message)
    resolveGameResponseFuture(askMessage.mapTo[GameResponseMessage])
  }

  private def resolveGameResponseFuture(result: Future[GameResponseMessage]): Route = {
    onComplete(result){
      case Failure(exception) =>
        complete(InternalServerError -> ServiceException("GameOperationError",s"Unexpected error processing operation: ${exception.getMessage}"))
      case Success(value) =>
        value match {
          case nf: GameOperationFailed => complete(nf.statusCode -> nf)
          case gms: GameState => complete(OK -> gms)
          case gmh: GameHistory => complete(OK -> gmh)
        }
    }
  }

}
