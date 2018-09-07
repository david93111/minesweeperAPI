package co.com.minesweeper.actor

import akka.actor.Actor
import akka.event.Logging
import co.com.minesweeper.util.BaseTimeout

/** Mixin for Actor using default Timeout and Actor logging
  *
  * @author david93111
  *
  * */
trait BaseActor extends Actor with BaseTimeout{

  val logger = Logging(context.system, this)

}
