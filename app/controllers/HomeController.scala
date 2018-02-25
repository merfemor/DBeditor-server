package controllers

import javax.inject._

import mail.{ConfirmEmailMessage, EmailManager}
import play.api.mvc._


@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               emailManager: EmailManager) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    emailManager.send(ConfirmEmailMessage("test@test.com", "123456"))
    Ok(views.html.main())
  }

}
