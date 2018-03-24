package controllers

import javax.inject._

import models.repository.{DatabaseRepository, UserRepository, UserRightRepository}
import play.api.mvc._


@Singleton
class HomeController @Inject()
(cc: ControllerComponents,
 val _userRepository: UserRepository,
 val _connectionRepository: DatabaseRepository,
 val _userRightRepository: UserRightRepository) extends AbstractController(cc) {

  Factory.userRepository = _userRepository
  Factory.connectionRepository = _connectionRepository
  Factory.userRightRepository = _userRightRepository

  def index() = Action {
    implicit request: Request[AnyContent] =>
      Ok(views.html.main())
  }
}