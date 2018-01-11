package auth

import javax.inject.Inject
import javax.persistence.EntityNotFoundException

import models.entity.User
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


class UserRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)


case class UserAction @Inject()(parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent] {

  import UserAction._

  override def invokeBlock[B](request: Request[B], block: UserRequest[B] => Future[Result]): Future[Result] = {
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
                  block(new UserRequest(user, request))
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
}

object UserAction {
  val UserIdCookieName = "session_user_id"
  val UserPasswordCookieName = "session_user_password"
}