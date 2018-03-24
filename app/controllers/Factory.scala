package controllers

import akka.actor.{ActorRef, ActorSystem}
import models.repository.{DatabaseRepository, UserRepository, UserRightRepository}
import websocket.NotifierActor

object Factory {
  lazy val actorSystem: ActorSystem = ActorSystem()
  lazy val notifier: ActorRef = actorSystem.actorOf(NotifierActor.props, "notifier-actor")
  var userRepository: UserRepository = _
  var connectionRepository: DatabaseRepository = _
  var userRightRepository: UserRightRepository = _
}
