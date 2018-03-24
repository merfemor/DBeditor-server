package controllers

import javax.inject.{Inject, Singleton}

import controllers.auth._
import models.entity.{SqlRight, UserRight}
import models.repository.UserRightRepository
import play.api.libs.json.Json
import play.api.mvc._

@Singleton
class UserRightController @Inject()(cc: ControllerComponents,
                                    UserAction: UserAction,
                                    userRightRepository: UserRightRepository,
                                    ConnectionCreatorAction: ConnectionCreatorAction,
                                    ConnectionUserAction: ConnectionUserAction)
  extends AbstractController(cc) {

  import UserRight._

  def ofUserInConnection(userId: Long, connectionId: Long) = UserAction {
    ConnectionCreatorAction(connectionId) { request: ConnectionRequest[AnyContent] =>
      Ok(Json.toJson(userRightRepository.rightsIn(userId, connectionId)))
    }
  }

  def updateRights(userId: Long, connectionId: Long) = UserAction(parse.json[Array[SqlRight]]) { userRequest: UserRequest[Array[SqlRight]] =>
    ConnectionUserAction(userRequest, connectionId, SqlRight.DCL) { _ =>
      userRequest.body.foreach(userRightRepository.grantRight(userId, connectionId, _))
      Ok(Json.toJson(userRightRepository.rightsIn(userId, connectionId)))
    }
  }
}
