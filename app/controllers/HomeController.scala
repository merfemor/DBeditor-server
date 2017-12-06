package controllers

import javax.inject._

import models.repository.TechSupportMessageRepository
import play.api.mvc._


@Singleton
class HomeController @Inject()(cc: ControllerComponents, techSupportMessageRepository: TechSupportMessageRepository)
  extends AbstractController(cc) {
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.main())
  }

  def techSupport() = Action { implicit request: Request[AnyContent] =>
    val messages = techSupportMessageRepository.all()
    Ok(messages.size.toString)
  }
}
