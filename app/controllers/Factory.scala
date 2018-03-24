package controllers

import akka.actor.ActorSystem
import models.repository.{DatabaseRepository, UserRepository, UserRightRepository}

object Factory {
  var actorSystem: ActorSystem = ActorSystem()
  var userRepository: UserRepository = _
  var connectionRepository: DatabaseRepository = _
  var userRightRepository: UserRightRepository = _
}
