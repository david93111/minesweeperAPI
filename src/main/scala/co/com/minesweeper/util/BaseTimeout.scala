package co.com.minesweeper.util

import akka.util.Timeout
import co.com.minesweeper.config.AppConf

import scala.concurrent.duration.{Duration, FiniteDuration}

trait BaseTimeout {

  val d: Duration = AppConf.defaultAskTimeoutActors
  implicit val timeout: Timeout = Timeout(FiniteDuration(d.length, d.unit))

}
