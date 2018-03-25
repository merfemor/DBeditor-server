package controllers

import javax.inject.{Inject, Singleton}

import controllers.auth._
import mail.{ConnectionRightsChangedEmail, EmailSender}
import models.entity.{SqlRight, UserRight}
import models.repository.{UserRepository, UserRightRepository}
import play.api.libs.json.Json
import play.api.mvc._

@Singleton
class UserRightController @Inject()(cc: ControllerComponents,
                                    UserAction: UserAction,
                                    userRightRepository: UserRightRepository,
                                    userRepository: UserRepository,
                                    ConnectionCreatorAction: ConnectionCreatorAction,
                                    ConnectionUserAction: ConnectionUserAction,
                                    emailSender: EmailSender)
  extends AbstractController(cc) {

  import UserRight._

  def ofUserInConnection(userId: Long, connectionId: Long) = UserAction {
    ConnectionCreatorAction(connectionId) { request: ConnectionRequest[AnyContent] =>
      Ok(Json.toJson(userRightRepository.rightsIn(userId, connectionId)))
    }
  }

  def updateRights(userId: Long, connectionId: Long) = UserAction(parse.json[Array[SqlRight]]) { userRequest: UserRequest[Array[SqlRight]] =>
    ConnectionUserAction(userRequest, connectionId, SqlRight.DCL) { connRequest =>
      userRepository.findById(userId).map { changedUser =>
        userRightRepository.clearRights(userId, connectionId)
        userRequest.body.foreach(right =>
          new UserRight(userId, connectionId, right).save()
        )
        val newRights = userRightRepository.rightsIn(userId, connectionId)
        emailSender.send(ConnectionRightsChangedEmail(
          changedUser.email,
          changedUser.username,
          userRequest.user.username,
          newRights,
          connRequest.dbConnection
        ))
        Ok(Json.toJson(newRights))
      } getOrElse {
        NotFound(s"No user with id = $userId")
      }
    }
  }
}
