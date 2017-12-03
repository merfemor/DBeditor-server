package controllers

import javax.inject._

import models.repository.UserRepository
import play.Logger
import play.api.mvc._

@Singleton
class HomeController @Inject()(cc: ControllerComponents, userRepository: UserRepository) extends AbstractController(cc) {
  def index() = Action { implicit request: Request[AnyContent] =>
    Logger.debug(userRepository.list.toString)
    Ok(views.html.main())
  }
}
