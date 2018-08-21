package co.com.minesweeper.config

import com.typesafe.config.{Config, ConfigFactory}

object AppConf {

  val appConfig: Config = ConfigFactory.load()

}
