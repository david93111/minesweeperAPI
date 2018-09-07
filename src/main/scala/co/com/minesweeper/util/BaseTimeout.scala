package co.com.minesweeper.util

import akka.util.Timeout
import co.com.minesweeper.config.AppConf

import scala.concurrent.duration.{Duration, FiniteDuration}

/**  Mix in for use of parametrized default timeout.
  *
  *  @author david93111
  *
  *  Uses default application timeout on application.conf to create an implicit akka Timeout
  */
trait BaseTimeout {

  val d: Duration = AppConf.defaultAskTimeoutActors
  implicit val timeout: Timeout = Timeout(FiniteDuration(d.length, d.unit))

}
