package co.com.minesweeper.config


import scala.collection.JavaConverters._
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable
import scala.concurrent.duration.Duration

/**  Factory for application configuration and exposure of general configuration values on Application.conf.
  *
  *  @author david93111
  *
  * */
object AppConf {

  val conf: Config = ConfigFactory.load("application")

  val serverConf: Config = conf.getConfig("http")
  val appHost: String = serverConf.getString("host")
  val appPort: Int = serverConf.getInt("port")

  val appConfig: Config = conf.getConfig("app")

  val defaultRows: Int = appConfig.getInt("minefield.default-rows")
  val defaultColumns: Int = appConfig.getInt("minefield.default-columns")
  val defaultMines: Int = appConfig.getInt("minefield.default-mines")

  val defaultAskTimeoutActors: Duration = Duration(appConfig.getString("default.ask-seconds-timeout"))

  val gameActorMaxIdleTime: Duration = Duration(appConfig.getString("default.game-actor.idle-max-time"))

  val allowedOrigins: mutable.Seq[String] = conf.getStringList("cors.allowed-origins").asScala

}
