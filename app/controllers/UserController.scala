package controllers

import javax.inject._

import auth.{UserAction, UserRequest}
import io.ebean.DuplicateKeyException
import models.entity.User
import models.repository._
import play.api.libs.json.Json
import play.api.mvc._


@Singleton
class UserController @Inject()(cc: ControllerComponents,
                               userRepository: UserRepository,
                               userRightsRepository: UserRightRepository,
                               UserAction: UserAction
                              )
  extends AbstractController(cc) {

  /*private val logger = Logger(getClass)


  def databaseUserInfo(userId: Long, databaseId: Long) = Action { implicit request: Request[AnyContent] =>
    val rights = userRightsRepository.rightsIn(userId, databaseId)
    Ok(rights.head.toString)
  }


  def verify() = Action { implicit request: Request[AnyContent] =>
    val info = userRepository.getUnverifiedInfo(1)
    if (info.isDefined) {
      Logger.debug(info.get.verificationCode)

      val info1 = userRepository.getUnverifiedInfo(info.get.verificationCode)
      Logger.debug(info1.get.verificationCode)
    }
    Ok(views.html.main())
  }*/


  def currentUserInfo() = UserAction { userRequest: UserRequest[AnyContent] =>
    Ok(Json.toJson(userRequest.user))
  }

  def register() = Action(parse.json[User]) { request: Request[User] =>
    val user = request.body
    try {
      user.save()
      // TODO: create verify info and send email
      Ok(Json.toJson(user))
    } catch {
      case e: DuplicateKeyException =>
        BadRequest(e.getMessage)
    }
  }

  def userInfo(id: Long) = UserAction {
    userRepository.findById(id).map(
      u => Ok(Json.toJson[User](u))
    ) getOrElse {
      NotFound(s"No user with id = $id")
    }
  }

  def search(query: Option[String], page: Int, pageSize: Int) = UserAction {
    query.map { query =>
      Ok(Json.toJson(userRepository.search(query, page, pageSize)))
    } getOrElse {
      Ok(Json.toJson(userRepository.all(page, pageSize)))
    }
  }

  def updateUserProfileInfo() = UserAction(parse.json[User](User.userReadsOptionFields)) { request: UserRequest[User] =>
    val newUser = request.body
    val curUser = request.user
    var somethingChanged = false

    if (newUser.username.nonEmpty) {
      somethingChanged = true
      curUser.username = newUser.username
    }
    if (newUser.password.nonEmpty) {
      somethingChanged = true
      curUser.password = newUser.password
    }
    if (newUser.email.nonEmpty) {
      somethingChanged = true
      curUser.email = newUser.email
      // TODO: update verify info and resend email
    }
    try {
      if (somethingChanged) {
        curUser.save()
      }
      Ok(Json.toJson(curUser))
    } catch {
      case e: DuplicateKeyException =>
        BadRequest(e.getMessage)
    }
  }

  def deleteCurrentUser() = UserAction { request: UserRequest[AnyContent] =>
    val user = request.user
    user.delete()
    Ok(Json.toJson(user))
  }
}