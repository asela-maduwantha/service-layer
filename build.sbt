ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "service-layer"
  )

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick"              % "3.4.1",
  "com.typesafe.slick" %% "slick-hikaricp"     % "3.4.1",
  "org.postgresql"     %  "postgresql"          % "42.6.0",
  "com.rabbitmq"       %  "amqp-client"        % "5.20.0",
  "org.slf4j"          %  "slf4j-simple"       % "2.0.16",
  "io.circe"          %% "circe-core"          % "0.14.5",
  "io.circe"          %% "circe-generic"       % "0.14.5",
  "io.circe"          %% "circe-parser"        % "0.14.5",
  "com.typesafe"       %  "config"             % "1.4.2",
  "com.zaxxer"         %  "HikariCP"           % "5.0.1",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.scalatestplus" %% "mockito-4-11" % "3.2.17.0" % Test,

)

import sbtassembly.AssemblyPlugin.autoImport._

enablePlugins(AssemblyPlugin)

Compile / mainClass := Some("Main")
assembly / assemblyJarName := "scala-app.jar"

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}