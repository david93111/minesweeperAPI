package co.com.minesweeper.actor

import akka.actor.{ActorRef, Props}
import akka.contrib.persistence.mongodb.{MongoReadJournal, ScalaDslMongoReadJournal}
import akka.pattern.{Backoff, BackoffSupervisor, ask, pipe}
import akka.persistence.query.scaladsl.{CurrentEventsByPersistenceIdQuery, CurrentEventsByTagQuery}
import akka.persistence.query.{EventEnvelope, PersistenceQuery, scaladsl}
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import co.com.minesweeper.actor.GameActor.{GetGameState, Snap}
import co.com.minesweeper.actor.GameManagerActor._
import co.com.minesweeper.api.services.MinefieldService
import co.com.minesweeper.model.error.GameOperationFailed
import co.com.minesweeper.model.messages.{GameBasicOperation, GameState}
import co.com.minesweeper.model.{GameStatus, MarkType, MinefieldConfig}

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class GameManagerActor()(implicit val ex: ExecutionContext, materializer: ActorMaterializer) extends BaseActor {

  /* JournalReader can fail if loaded on startup, due to plugin not fully loaded and connection with MongoDB
  // May not be established correctly just after bootUp, load on first successful request is used instead */
  var journal: Option[GameManagerReadJournal] = None

  override def receive: Receive = {
    case CreateGame(gameId, conf, username) =>
      val newGame = GameState(gameId, GameStatus.Active, username, MinefieldService.createMinefield(conf.columns, conf.rows, conf.mines))
      val actorRef = createNewGameOnRouter(gameId, newGame)
      actorRef ! Snap
      actorRef.forward(GetGameState)

    case GetGame(actorId) =>
      val fGetGame = { child: ActorRef =>
        child ? GetGameState
      }
      sendToChild(actorId, fGetGame, sender)

    case SendRevealSpot(actorId, row, col) =>
      val fRevealSpot = { child: ActorRef =>
        child ? GameActor.RevealSpot(row, col)
      }
      sendToChild(actorId, fRevealSpot, sender)

    case SendMarkSpot(actorId, row, col, mark: MarkType) =>
      val fMarkSpot = { child: ActorRef =>
        child ? GameActor.MarkSpot (row, col, mark)
      }
      sendToChild(actorId, fMarkSpot, sender)
    case SendGameMessage(actorId, message) =>
      val fAskMessage = { child: ActorRef =>
        child ? message
      }
      sendToChild(actorId, fAskMessage, sender)
  }

  def createNewGameOnRouter(gameId: String, game: GameState): ActorRef ={
    val props: Props = BackoffSupervisor.props(
      Backoff.onFailure(
        GameActor.props(gameId, game),
        childName = gameId,
        minBackoff = 5.seconds,
        maxBackoff = 45.seconds,
        randomFactor = 0.2
      )
    )
    val actorRef: ActorRef = context.actorOf(props, name = gameId)
    actorRef
  }

  def loadJournal: Option[GameManagerReadJournal] = {
    if(journal.isEmpty) {
      journal = Try(
        PersistenceQuery(context.system).readJournalFor[ScalaDslMongoReadJournal](MongoReadJournal.Identifier)
      ).toOption
    }
    journal
  }

  def journalQueryEvents(journal: GameManagerReadJournal, actorId: String): Future[immutable.Seq[EventEnvelope]] = {
    journal.currentEventsByPersistenceId(actorId, 0L, Long.MaxValue).runWith(Sink.seq)
  }

  private def sendToChild(actorId: String, functionOnFound: ActorRef => Future[Any], sender: ActorRef): Unit = {
    context.child(actorId).fold {
      val res: Future[Any] = validateActorOnJournalAndSendMessage(actorId, sender, functionOnFound)
      res.pipeTo(sender)
    }(actor =>
      functionOnFound(actor).pipeTo(sender)
    )
  }

  def validateActorOnJournalAndSendMessage(actorId: String, sender: ActorRef, functionOnFound: ActorRef => Future[Any]): Future[Any] ={
    loadJournal.fold[Future[Any]]{
      logger.error("Exception trying to access the journal for query read, cause: Journal could't be loaded")
      Future.successful(
        GameOperationFailed.gameNotFound(actorId, "Game is not on memory and journal is not available, please try again later")
      )
    } { journal =>
      journalQueryEvents(journal, actorId).flatMap { events =>
        events.reverse.collectFirst{
          case EventEnvelope(_, _, _, event) if event.isInstanceOf[GameState] =>
            event.asInstanceOf[GameState]
        }.fold[Future[Any]](
          Future.successful(
            GameOperationFailed.gameNotFound(actorId, "Game does not exist on akka_journal or perhaps corrupted, please create a new game")
          )
        ) { validState =>
          logger.debug("Game with id: {} founded", actorId)
          val ref = createNewGameOnRouter(actorId, validState)
          functionOnFound(ref)
        }
      }
    }
  }

}

/** Factory of GameManager Router Props, contain possible DSL to interact with the GameManager actor */
object GameManagerActor{

  /** custom abbreviated type for specify required journal  abstraction for execution or testing  */
  type GameManagerReadJournal = scaladsl.ReadJournal with CurrentEventsByPersistenceIdQuery with CurrentEventsByTagQuery

  /** messages implemented using ConsistentHashtable, this is going to be useful to add clustering with sharding
    * in near future changing this for a shard region implementation with extract entityId
    */
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
  case class SendGameMessage(gameId: String, message: GameBasicOperation) extends ConsistentHashable{
    override def consistentHashKey: Any = gameId
  }

  def props()(implicit ex: ExecutionContext, materializer: ActorMaterializer): Props = {
    Props(new GameManagerActor()(ex, materializer))
  }

}
