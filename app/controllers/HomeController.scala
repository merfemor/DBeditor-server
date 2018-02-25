package controllers

import javax.inject._

import mail.EmailPublisher
import play.api.mvc._


@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               emailSender: EmailPublisher) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    emailSender.send(null)
    Ok(views.html.main())
  }

}
