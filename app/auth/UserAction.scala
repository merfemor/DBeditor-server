package auth

import javax.persistence.EntityNotFoundException

import models.entity.User
import play.api.mvc._

import scala.concurrent.Future
import scala.util.Try


class UserRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)


case class UserAction[A](action: Action[A]) extends Action[A] {

  import UserAction._

  def apply(request: Request[A]): Future[Result] = {
    request.cookies.get(UserIdCookieName) match {
      case Some(userId) =>
        Try(userId.value.toLong).toOption match {
          case Some(id) =>
            request.cookies.get(UserPasswordCookieName) match {
              case Some(password) =>
                val user = new User
                user.id = id
                try {
                  user.refresh()
                } catch {
                  case _: EntityNotFoundException =>
                    return Future.successful(Results.UnprocessableEntity(s"No user with id = $id"))
                }
                if (password.value == user.password) {
                  action(new UserRequest(user, request))
                } else {
                  Future.successful(Results.UnprocessableEntity("Wrong password"))
                }
              case None => Future.successful(Results.BadRequest("No \"" + UserPasswordCookieName + "\" cookie set"))
            }
          case None => Future.successful(Results.UnprocessableEntity("Bad \"" + UserIdCookieName + "\" cookie value"))
        }
      case None => Future.successful(Results.Unauthorized("No \"" + UserIdCookieName + "\" cookie set"))
    }
  }

  override def parser = action.parser

  override def executionContext = action.executionContext
}

object UserAction {
  val UserIdCookieName = "session_user_id"
  val UserPasswordCookieName = "session_user_password"
}