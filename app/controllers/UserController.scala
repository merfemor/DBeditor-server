package controllers

import javax.inject._

import models.repository._
import play.Logger
import play.api.mvc._


@Singleton
class UserController @Inject()(cc: ControllerComponents,
                               userRepository: UserRepository)
  extends AbstractController(cc) {


  def list() = Action { implicit request: Request[AnyContent] =>
    val users = userRepository.page(0, 1)

    val firstUser = users.get(0)
    Logger.debug("1 id = " + firstUser.id)

    val copyUser = userRepository.findById(firstUser.id)
    Logger.debug("2 id = " + copyUser.id)

    Logger.debug((copyUser == firstUser).toString)

    Ok(views.html.main())
  }
}
