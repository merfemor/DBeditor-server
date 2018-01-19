package auth

import javax.inject._

import models.entity.Database
import models.repository._
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

case class ConnectionCreatorRequest[A](dbConnection: Database, request: Request[A]) extends WrappedRequest[A](request)

case class ConnectionCreatorAction @Inject()(databaseRepository: DatabaseRepository) {

  def apply[A](request: UserRequest[A], connectionId: Long)
              (block: ConnectionCreatorRequest[A] => Result) = {
    databaseRepository.findById(connectionId).map { db =>
      if (db.creator.id == request.user.id) {
        block(ConnectionCreatorRequest(db, request))
      } else {
        Forbidden(s"Connection with id = $connectionId created by another user")
      }
    } getOrElse {
      NotFound(s"No connection with id = $connectionId")
    }
  }

  def apply[A](connectionId: Long)
              (block: ConnectionCreatorRequest[A] => Result): UserRequest[A] => Result = { request: UserRequest[A] =>
    apply(request, connectionId)(block)
  }

}

