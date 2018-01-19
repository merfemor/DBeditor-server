package controllers

import javax.inject.{Inject, Singleton}

import auth.{ConnectionCreatorAction, ConnectionCreatorRequest, UserAction, UserRequest}
import io.ebean.{DataIntegrityException, DuplicateKeyException}
import models.entity.UserRight
import models.repository.UserRightRepository
import play.api.libs.json.Json
import play.api.mvc._

@Singleton
class UserRightController @Inject()(cc: ControllerComponents,
                                    UserAction: UserAction,
                                    userRightRepository: UserRightRepository,
                                    ConnectionCreatorAction: ConnectionCreatorAction)
  extends AbstractController(cc) {

  def grantRight = UserAction(parse.json[UserRight]) { userRequest: UserRequest[UserRight] =>
    val right = userRequest.body
    ConnectionCreatorAction(userRequest, right.databaseId) { request: ConnectionCreatorRequest[UserRight] =>
      if (right.userId == request.dbConnection.creator.id) {
        BadRequest("Unable to grant any rights to the creator himself")
      } else {
        try {
          right.save()
          Ok(Json.toJson(right))
        } catch {
          case _: DataIntegrityException =>
            NotFound("No such user or connection")
          case _: DuplicateKeyException =>
            Ok(Json.toJson(right)) // This user already has this right
        }
      }
    }
  }

  def ofUserInConnection(userId: Long, connectionId: Long) = UserAction {
    import UserRight._
    ConnectionCreatorAction(connectionId) { request: ConnectionCreatorRequest[AnyContent] =>
      Ok(Json.toJson(userRightRepository.rightsIn(userId, connectionId)))
    }
  }
}
