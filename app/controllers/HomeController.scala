package controllers

import javax.inject._

import mail.ConfirmEmailMessage
import play.api.mvc._
import queue.QueueMessagePublisher


@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               queuePublisher: QueueMessagePublisher) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    queuePublisher.publish(new ConfirmEmailMessage("test@test.com", "123456"))
    Ok(views.html.main())
  }

}
