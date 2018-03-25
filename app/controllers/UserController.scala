package controllers

import java.net.{MalformedURLException, URL}
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject._

import controllers.auth.{UserAction, UserRequest}
import io.ebean.DuplicateKeyException
import mail.{ConfirmEmailMessage, EmailSender}
import models.entity.{UnverifiedUserInfo, User}
import models.repository._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import util.LogUtils

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

  def currentUserInfo() = UserAction { userRequest: UserRequest[AnyContent] =>
    Ok(Json.toJson(userRequest.user))
  }

  def register(verificationPageLink: String) = Action(parse.json[User]) { request: Request[User] =>
    LogUtils.logRequest(request)
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

  def patchUser() = UserAction(parse.json[User](User.userReadsOptionFields)) {
    request: UserRequest[User] =>
      val newUser = request.body
      val curUser = request.user
      var somethingChanged = false

      if (newUser.username.nonEmpty && newUser.username != curUser.username) {
        somethingChanged = true
        curUser.username = newUser.username
      }
      if (newUser.password.nonEmpty && newUser.password != curUser.password) {
        somethingChanged = true
        curUser.password = newUser.password
      }
      if (newUser.email.nonEmpty && newUser.email != curUser.email) {
        somethingChanged = true
        curUser.email = newUser.email
        val info = userRepository.getUnverifiedInfo(curUser.id).getOrElse {
          val i = new UnverifiedUserInfo
          i.user = curUser
          i
        }
        info.verificationCode = randomVerificationCode
        info.save()
        val url = new URL(s"http://localhost:9000/verify?code=${info.verificationCode}")
        emailSender.send(ConfirmEmailMessage(curUser.email, curUser.username, url))
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
    Ok(Json.toJson(userRepository.deleteById(request.user.id)))
  }

  def verifyEmail(code: String) = Action {
    Logger.info(s"GET /verify?code=$code")
    userRepository.getUnverifiedInfo(code) match {
      case Some(info) =>
        val timeDiff = new Date().getTime - info.registrationDate.getTime
        info.deletePermanent()
        if (TimeUnit.HOURS.convert(timeDiff, TimeUnit.MILLISECONDS) < 24) {
          Ok("")
        } else {
          NotFound("no such verification code")
        }
      case None =>
        NotFound("no such verification code")
    }
  }
}

object UserController {
  private lazy val random = new Random

  def randomVerificationCode: String = random.alphanumeric.take(40).mkString
}