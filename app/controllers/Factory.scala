package controllers

import akka.actor.{ActorRef, ActorSystem}
import models.repository.{DatabaseRepository, UserRepository, UserRightRepository}
import websocket.{NotifierActor, SqlParseActor}

object Factory {
  lazy val actorSystem: ActorSystem = ActorSystem()
  lazy val notifier: ActorRef = actorSystem.actorOf(NotifierActor.props, "notifier-actor")
  lazy val parser: ActorRef = actorSystem.actorOf(SqlParseActor.props, "sql-parse-actor")
  var userRepository: UserRepository = _
  var connectionRepository: DatabaseRepository = _
  var userRightRepository: UserRightRepository = _
}
