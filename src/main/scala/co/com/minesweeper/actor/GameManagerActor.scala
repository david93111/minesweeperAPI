package co.com.minesweeper.actor

import akka.actor.{ActorRef, Props}
import akka.contrib.persistence.mongodb.{MongoReadJournal, ScalaDslMongoReadJournal}
import akka.pattern.{Backoff, BackoffSupervisor, ask, pipe}
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import co.com.minesweeper.actor.GameActor.{GetMinefield, Snap}
import co.com.minesweeper.actor.GameManagerActor._
import co.com.minesweeper.api.services.MinefieldService
import co.com.minesweeper.model.error.GameOperationFailed
import co.com.minesweeper.model.{GameState, GameStatus, MarkType, MinefieldConfig}

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class GameManagerActor()(implicit val ex: ExecutionContext, materializer: ActorMaterializer) extends BaseActor {

  override def receive: Receive = {
    case CreateGame(gameId, conf, username) =>
      val newGame = GameState(gameId, GameStatus.Active, username, MinefieldService.createMinefield(conf.columns, conf.rows, conf.mines))
      val actorRef = createNewGameOnRouter(gameId, newGame)
      actorRef ! Snap
      actorRef.forward(GetMinefield)

    case GetGame(actorId) =>
      val fGetGame = { child: ActorRef =>
        child.forward(GetMinefield)
      }
      sendToChild(actorId, fGetGame, sender)

    case SendRevealSpot(actorId, row, col) =>
      val fRevealSpot = { child: ActorRef =>
        child.forward(GameActor.RevealSpot(row, col))
      }
      sendToChild(actorId, fRevealSpot, sender)

    case SendMarkSpot(actorId, row, col, mark: MarkType) =>
      val fMarkSpot = { child: ActorRef =>
          child.forward(GameActor.MarkSpot (row, col, mark))
      }
      sendToChild(actorId, fMarkSpot, sender)
  }

  def createNewGameOnRouter(gameId: String, game: GameState): ActorRef ={
    val props: Props = BackoffSupervisor.props(
      Backoff.onStop(
        GameActor.props(gameId, game),
        childName = gameId,
        minBackoff = 3.seconds,
        maxBackoff = 30.seconds,
        randomFactor = 0.2
      )
    )
    val actorRef: ActorRef = context.actorOf(props, name = gameId)
    actorRef
  }

  def loadJournal: Try[ScalaDslMongoReadJournal] = {
    Try(PersistenceQuery(context.system).readJournalFor[ScalaDslMongoReadJournal](MongoReadJournal.Identifier))
  }

  def journalQueryEvents(journal: ScalaDslMongoReadJournal, actorId: String): Future[immutable.Seq[EventEnvelope]] = {
    journal.currentEventsByPersistenceId(actorId, 0L, Long.MaxValue).runWith(Sink.seq)
  }

  private def sendToChild(actorId: String, functionOnFound: ActorRef => Unit, sender: ActorRef): Unit = {
    context.child(actorId).fold {
      validateActorOnJournalAndSendMessage(actorId, sender, functionOnFound)
    }(actor =>
      functionOnFound(actor)
    )
  }

  def validateActorOnJournalAndSendMessage(actorId: String, sender: ActorRef, functionOnFound: ActorRef => Unit): Unit ={
    loadJournal.fold( ex =>{
      logger.error("Exception trying to access the journal for query read, cause: {}", ex.getMessage)
      sender ! GameOperationFailed.gameNotFound(actorId, "Actor is not on memory and journal is not available, please try again later")
    },journal =>
      journalQueryEvents(journal, actorId).onComplete {
        case Success(value) if value.nonEmpty =>
          logger.debug(s"The journal registries acquired are : ${value.mkString("|-|")}")
          value.reverse.map{ event =>
            event.event match{
              case game: GameState =>
                Option(game)
              case _ =>
                None
            }
          }.find(_.isDefined).flatten.fold(
            sender ! GameOperationFailed.gameNotFound(actorId, "Actor not recoverable from akka_journal, perhaps corrupted, please create a new one")
          ){ state =>
            logger.debug("Actor with id: {} founded", actorId)
            val ref = createNewGameOnRouter(actorId, state)
            logger.debug("Actor with id: {} created", actorId)
            functionOnFound(ref)
          }
        case _ =>
          sender ! GameOperationFailed.gameNotFound(actorId, "Actor not found on memory or akka_journal, please create a new one")
      }
    )
  }
}

object GameManagerActor{

  // Implemented using ConsistentHashtable, this is going to be useful to add sharding in near future
  case class CreateGame(gameId: String, minefieldConf: MinefieldConfig, username: String) extends ConsistentHashable{
    override def consistentHashKey: Any = gameId
  }
  case class GetGame(gameId: String) extends ConsistentHashable{
    override def consistentHashKey: Any = gameId
  }
  case class SendRevealSpot(gameId: String, row: Int, col: Int) extends ConsistentHashable{
    override def consistentHashKey: Any = gameId
  }
  case class SendMarkSpot(gameId: String, row: Int, col: Int, mark: MarkType) extends ConsistentHashable{
    override def consistentHashKey: Any = gameId
  }

  def props()(implicit ex: ExecutionContext, materializer: ActorMaterializer): Props = {
    Props(new GameManagerActor()(ex, materializer))
  }

}
