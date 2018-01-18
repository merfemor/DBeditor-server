package auth

import javax.inject.Inject

import models.entity.User
import models.repository.UserRepository
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


case class UserRequest[A](user: User, request: Request[A]) extends WrappedRequest[A](request)


case class UserAction @Inject()(parser: BodyParsers.Default, userRepository: UserRepository)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent] {

  import UserAction._
  import Results._

  override def invokeBlock[B](request: Request[B], block: UserRequest[B] => Future[Result]): Future[Result] = {
    request.cookies.get(UserUsernameCookieName) match {
      case Some(username) =>
        request.cookies.get(UserPasswordCookieName) match {
          case Some(password) =>
            userRepository.findByUsername(username.value).map { user =>
              if (password.value == user.password) {
                block(UserRequest(user, request))
              } else {
                Future.successful(BadRequest("Error in cookie \"" + UserPasswordCookieName+ "\": wrong password"))
              }
            } getOrElse {
              Future.successful(BadRequest("Error in cookie \"" + UserUsernameCookieName + "\": no user \"" + username.value + "\""))
            }
          case None => Future.successful(Unauthorized("No \"" + UserPasswordCookieName + "\" cookie set"))
        }
      case None => Future.successful(Unauthorized("No \"" + UserUsernameCookieName + "\" cookie set"))
    }
  }
}

object UserAction {
  val UserUsernameCookieName = "session_username"
  val UserPasswordCookieName = "session_password"
}