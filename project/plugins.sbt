logLevel := Level.Warn

resolvers += Classpaths.typesafeReleases

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.6")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.1")
