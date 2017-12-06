package controllers

import javax.inject._

import models.repository._
import play.Logger
import play.api.mvc._


@Singleton
class UserController @Inject()(cc: ControllerComponents,
                               userRepository: UserRepository,
                               userRightsRepository: UserRightRepository)
  extends AbstractController(cc) {


  def list() = Action { implicit request: Request[AnyContent] =>
    val users = userRepository.all(0, 1)

    val firstUser = users.get(0)
    Logger.debug("1 id = " + firstUser.id)

    val copyUser = userRepository.findById(firstUser.id)
    Logger.debug("2 id = " + copyUser.id)

    Logger.debug((copyUser == firstUser).toString)

    Ok(views.html.main())
  }


  def databaseUserInfo(userId: Long, databaseId: Long) = Action { implicit request: Request[AnyContent] =>
    val rights = userRightsRepository.rightsIn(userId, databaseId)
    Ok(rights.head.toString)
  }


  def search() = Action { implicit request: Request[AnyContent] =>
    val query = "test"
    val users = userRepository.search(query, 0, 3)
    val user = users.headOption
    if (user.isDefined)
      Ok(user.get.username)
    else
      Ok("[]")
  }
}
