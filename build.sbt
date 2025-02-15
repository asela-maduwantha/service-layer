ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "service-layer"
  )


libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick"              % "3.3.3",
  "com.typesafe.slick" %% "slick-hikaricp"     % "3.3.3",
  "org.postgresql"     %  "postgresql"         % "9.4-1206-jdbc42",
  "com.rabbitmq" % "amqp-client" % "5.5.0" ,
  "org.slf4j" % "slf4j-simple" % "2.0.16",
  "io.circe" %% "circe-core" % "0.14.5",
  "io.circe" %% "circe-generic" % "0.14.5",
  "io.circe" %% "circe-parser" % "0.14.5",
  "com.typesafe" % "config" % "1.4.2",
  "com.zaxxer" % "HikariCP" % "5.0.0"
)



enablePlugins(AssemblyPlugin)

mainClass in assembly := Some("Main")

assemblyJarName in assembly := "scala-app.jar"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}


