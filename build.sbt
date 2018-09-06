name := "minesweeperms"

version := "1.0.0Beta1"

scalaVersion := "2.12.6"

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.co.minesweeper.api"
  )

// AKKA
val akkaHttpVersion = "10.1.3"
val akkaVersion = "2.5.14"
val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-stream",
  "com.typesafe.akka" %% "akka-actor",
  "com.typesafe.akka" %% "akka-persistence",
  "com.typesafe.akka" %% "akka-persistence-query",
  "com.typesafe.akka" %% "akka-slf4j"
).map(_ % akkaVersion)

// CIRCE
val circeVersion = "0.9.3"
val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

// Reactive Mongo version
val rxMongoVersion="0.12.3"

val generalDependencies = Seq(
  "org.scalatest"          %%   "scalatest"                      % "3.0.5"          % Test,
  "com.typesafe.akka"      %%   "akka-testkit"                   % "2.5.14"         % Test,
  "com.github.dnvriend"    %%   "akka-persistence-inmemory"      % "2.5.1.1"        % Test,
  "com.typesafe.akka"      %%   "akka-http-testkit"              % akkaHttpVersion  % Test,
  "com.typesafe.akka"      %%   "akka-http"                      % akkaHttpVersion,
  "de.heikoseeberger"      %%   "akka-http-circe"                % "1.21.0",
  "ch.qos.logback"         %    "logback-classic"                % "1.2.3",
  "com.typesafe"           %    "config"                         % "1.3.2",
  "com.beachape"           %%   "enumeratum-circe"               % "1.5.13",
  "com.github.scullxbones" %%   "akka-persistence-mongo-rxmongo" % "2.0.11",
  "org.reactivemongo"      %%   "reactivemongo"                  % rxMongoVersion,
  "org.reactivemongo"      %%   "reactivemongo-akkastream"       % rxMongoVersion,
  "com.github.romix.akka"  %%   "akka-kryo-serialization"        % "0.5.2"
)

parallelExecution in Test:=false

libraryDependencies ++= akkaDependencies ++ circeDependencies ++ generalDependencies

mainClass in Compile := Some("co.com.minesweeper.startup.Boot")

enablePlugins(JavaServerAppPackaging)

coverageMinimum := 80
coverageFailOnMinimum := true
coverageEnabled in Universal:= true

packageName in Universal := name.value