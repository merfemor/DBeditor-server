name := """dbeditor-server"""
organization := "ru.ifmo.se"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, PlayEbean)

scalaVersion := "2.12.3"


libraryDependencies ++= Seq(
  guice,
  jdbc,
  "org.postgresql" % "postgresql" % "9.4.1211",
  "com.rabbitmq" % "amqp-client" % "5.1.1",
  "com.newmotion" %% "akka-rabbitmq" % "5.0.0",
  "javax.mail" % "mail" % "1.4.7"
)