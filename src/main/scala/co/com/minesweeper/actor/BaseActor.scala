package co.com.minesweeper.actor

import akka.actor.Actor
import akka.dispatch.MessageDispatcher
import akka.event.Logging
import akka.util.Timeout

import scala.concurrent.duration.{ Duration, FiniteDuration }

trait BaseActor extends Actor{

  val logger = Logging(context.system, this)
  val d = Duration("10s")
  implicit val timeout: Timeout = Timeout(FiniteDuration(d.length, d.unit))

}
