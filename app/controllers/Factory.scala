package controllers

import models.repository.{DatabaseRepository, UserRepository, UserRightRepository}

object Factory {
  var userRepository: UserRepository = _
  var connectionRepository: DatabaseRepository = _
  var userRightRepository: UserRightRepository = _
}
