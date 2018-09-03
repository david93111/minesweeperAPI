package co.com.minesweeper.actor

import akka.actor.{ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import co.com.minesweeper.actor.GameActor.GetMinefield
import co.com.minesweeper.actor.GameManagerActor._
import co.com.minesweeper.model.error.GameOperationFailed
import co.com.minesweeper.model.{MarkType, MinefieldConfig}

import scala.concurrent.{ExecutionContext, Future}

class GameManagerActor()(implicit val ex: ExecutionContext) extends BaseActor {

  override def receive: Receive = {
    case CreateGame(gameInMemoryId, conf, username) =>
      val actorRef: ActorRef = context.actorOf(GameActor.props(gameInMemoryId, conf, username), name = gameInMemoryId)
      context.watch(actorRef)
      (actorRef ? GetMinefield).pipeTo(sender())

    case GetGame(actorId) =>
      val fGetGame = { child: ActorRef =>
        (child ? GetMinefield).pipeTo(sender())
      }
      sendToChild[Future](actorId, fGetGame, sender())

    case SendRevealSpot(actorId, row, col) =>
      val fRevealSpot = { child: ActorRef =>
        (child ? GameActor.RevealSpot(row, col)).pipeTo(sender())
      }
      sendToChild[Future](actorId, fRevealSpot, sender())

    case SendMarkSpot(actorId, row, col, mark: MarkType) =>
      val fMarkSpot = { child: ActorRef =>
          (child ? GameActor.MarkSpot (row, col, mark)).pipeTo(sender())
      }
      sendToChild[Future](actorId, fMarkSpot, sender())
  }

  private def sendToChild[F[_]](actorId: String, functionOnFound: ActorRef => F[_], sender: ActorRef): Unit = {
    context.child(actorId).fold(
      sender ! GameOperationFailed.gameNotFound(actorId)
    )(actor =>
      functionOnFound(actor)
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

  def props()(implicit ex: ExecutionContext): Props = {
    Props(new GameManagerActor()(ex))
  }

}
