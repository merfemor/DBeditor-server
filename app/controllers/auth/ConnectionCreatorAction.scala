package controllers.auth

import javax.inject._

import models.entity.{Database, SqlRight}
import models.repository._
import play.api.mvc.Results._
import play.api.mvc._

case class ConnectionRequest[A](dbConnection: Database, request: Request[A], rights: Seq[SqlRight]) extends WrappedRequest[A](request)

case class ConnectionCreatorAction @Inject()(databaseRepository: DatabaseRepository) {

  def apply[A](request: UserRequest[A], connectionId: Long)
              (block: ConnectionRequest[A] => Result) = {
    databaseRepository.findById(connectionId).map { db =>
      if (db.creator.id == request.user.id) {
        block(ConnectionRequest(db, request, Seq(SqlRight.DCL)))
      } else {
        Forbidden(s"Connection with id = $connectionId created by another user")
      }
    } getOrElse {
      NotFound(s"No connection with id = $connectionId")
    }
  }

  def apply[A](connectionId: Long)
              (block: ConnectionRequest[A] => Result): UserRequest[A] => Result = { request: UserRequest[A] =>
    apply(request, connectionId)(block)
  }
}

case class ConnectionUserAction @Inject()(databaseRepository: DatabaseRepository, userRightRepository: UserRightRepository) {

  def apply[A](request: UserRequest[A], connectionId: Long, right: SqlRight)
              (block: ConnectionRequest[A] => Result): Result = {
    databaseRepository.findById(connectionId).map { db =>
      if (db.creator.id == request.user.id) {
        block(ConnectionRequest(db, request, Seq(SqlRight.DCL)))
      } else {
        val rights = userRightRepository.rightsIn(request.user.id, connectionId)
        if (rights.exists(SqlRight.isIncludes(right, _))) {
          block(ConnectionRequest(db, request, rights))
        } else {
          Forbidden(s"Connection with id = $connectionId created by another user")
        }
      }
    } getOrElse {
      NotFound(s"No connection with id = $connectionId")
    }
  }

  def apply[A](connectionId: Long, right: SqlRight)
              (block: ConnectionRequest[A] => Result): UserRequest[A] => Result = { request: UserRequest[A] =>
    apply(request, connectionId, right)(block)
  }

}

