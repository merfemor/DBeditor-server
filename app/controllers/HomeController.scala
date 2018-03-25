package controllers

import javax.inject._

import models.repository.{DatabaseRepository, TechSupportRepository, UserRepository, UserRightRepository}
import play.api.mvc._


@Singleton
class HomeController @Inject()
(cc: ControllerComponents,
 val _userRepository: UserRepository,
 val _connectionRepository: DatabaseRepository,
 val _userRightRepository: UserRightRepository,
 val _techSupportRepository: TechSupportRepository) extends AbstractController(cc) {

  Factory.userRepository = _userRepository
  Factory.connectionRepository = _connectionRepository
  Factory.userRightRepository = _userRightRepository
  Factory.techSupportRepository = _techSupportRepository

  def index() = Action {
      Ok(views.html.main())
  }
}