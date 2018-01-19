package controllers

import javax.inject._

import mail.{EmailPublisher, RabbitMQConfig}
import play.api.mvc._


@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               rabbitMQConfig: RabbitMQConfig) extends AbstractController(cc) {
  val emailSender: EmailPublisher = EmailPublisher(rabbitMQConfig)

  def index() = Action { implicit request: Request[AnyContent] =>
    emailSender.send(null)
    Ok(views.html.main())
  }

}
