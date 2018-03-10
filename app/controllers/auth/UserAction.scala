package controllers.auth

import javax.inject.Inject

import models.entity.User
import models.repository.UserRepository
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


case class UserRequest[A](user: User, request: Request[A]) extends WrappedRequest[A](request)


case class UserAction @Inject()(parser: BodyParsers.Default, userRepository: UserRepository)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent] {

  import Results._
  import UserAction._

  override def invokeBlock[B](request: Request[B], block: UserRequest[B] => Future[Result]): Future[Result] = {
    invokeCheckError(request).fold(Future.successful, u => block(UserRequest(u, request)))
  }

  def invokeCheckError[A](request: RequestHeader): Either[Result, User] = {
    request.cookies.get(UserUsernameCookieName) match {
      case Some(username) =>
        request.cookies.get(UserPasswordCookieName) match {
          case Some(password) =>
            userRepository.findByUsername(username.value).map { user =>
              if (password.value == user.password) {
                Right(user)
              } else {
                Left(Forbidden("Error in cookie \"" + UserPasswordCookieName + "\": wrong password"))
              }
            } getOrElse {
              Left(Forbidden("Error in cookie \"" + UserUsernameCookieName + "\": no user \"" + username.value + "\""))
            }
          case None => Left(Unauthorized("No \"" + UserPasswordCookieName + "\" cookie set"))
        }
      case None => Left(Unauthorized("No \"" + UserUsernameCookieName + "\" cookie set"))
    }
  }
}

object UserAction {
  val UserUsernameCookieName = "session_username"
  val UserPasswordCookieName = "session_password"
}