package controllers

import java.net.{MalformedURLException, URL}
import java.util.Date
import javax.inject._

import auth.{UserAction, UserRequest}
import io.ebean.DuplicateKeyException
import mail.{ConfirmEmailMessage, EmailSender}
import models.entity.{UnverifiedUserInfo, User}
import models.repository._
import play.api.libs.json.Json
import play.api.mvc._

import scala.util.Random


@Singleton
class UserController @Inject()(cc: ControllerComponents,
                               userRepository: UserRepository,
                               userRightsRepository: UserRightRepository,
                               UserAction: UserAction,
                               emailSender: EmailSender
                              )
  extends AbstractController(cc) {

  import UserController._

  /*
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

  def register(verificationPageLink: String) = Action(parse.json[User]) { request: Request[User] =>
    val user = request.body
    val info = new UnverifiedUserInfo()
    info.user = user
    info.verificationCode = randomVerificationCode
    info.registrationDate = new Date()

    try {
      val url = new URL(verificationPageLink + info.verificationCode)
      user.save()
      info.save()
      emailSender.send(ConfirmEmailMessage(user.email, user.username, url))
      Ok(Json.toJson(user))
    } catch {
      case e: DuplicateKeyException =>
        BadRequest(s"Duplicate user data: ${e.getMessage}")
      case e: MalformedURLException =>
        BadRequest(s"Bad url: ${e.getMessage}")
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
    query.map {
      query =>
        Ok(Json.toJson(userRepository.search(query, page, pageSize)))
    } getOrElse {
      Ok(Json.toJson(userRepository.all(page, pageSize)))
    }
  }


  def updateUserProfileInfo() = UserAction(parse.json[User](User.userReadsOptionFields)) {
    request: UserRequest[User] =>
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
        val info = userRepository.getUnverifiedInfo(newUser.id).getOrElse {
          val i = new UnverifiedUserInfo
          i.user = newUser
          i
        }
        info.verificationCode = randomVerificationCode
        info.save()
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

  def deleteCurrentUser() = UserAction {
    request: UserRequest[AnyContent] =>
      val user = request.user
      user.delete()
      Ok(Json.toJson(user))
  }
}

object UserController {
  private val random = new Random

  def randomVerificationCode: String = random.alphanumeric.take(40).mkString
}