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

    val firstUser = users.head
    Logger.debug("1 id = " + firstUser.id)

    val copyUser = userRepository.findById(firstUser.id)

    if (copyUser.isDefined) {
      Logger.debug("2 id = " + copyUser.get.id)

      Logger.debug((copyUser.get == firstUser).toString)
    }
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


  def verify() = Action { implicit request: Request[AnyContent] =>
    val info = userRepository.getUnverifiedInfo(1)
    if (info.isDefined) {
      Logger.debug(info.get.verificationCode)

      val info1 = userRepository.getUnverifiedInfo(info.get.verificationCode)
      Logger.debug(info1.get.verificationCode)
    }
    Ok(views.html.main())
  }
}
