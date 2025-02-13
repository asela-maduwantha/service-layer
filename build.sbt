ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "service-layer"
  )


libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick"              % "3.3.3",
  "com.typesafe.slick" %% "slick-hikaricp"     % "3.3.3",
  "org.postgresql"     %  "postgresql"         % "42.5.0",
  "com.rabbitmq" % "amqp-client" % "5.5.0" ,
  "org.slf4j" % "slf4j-simple" % "1.7.30",
  "io.circe" %% "circe-core" % "0.14.5",
  "io.circe" %% "circe-generic" % "0.14.5",
  "io.circe" %% "circe-parser" % "0.14.5",
  "com.typesafe" % "config" % "1.4.2",

)


