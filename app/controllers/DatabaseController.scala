package controllers

import javax.inject.{Inject, Singleton}

import controllers.auth._
import models.entity.{Database, SqlRight}
import models.repository.{DatabaseRepository, UserRightRepository}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents}
import util.DbUtils

@Singleton
class DatabaseController @Inject()(cc: ControllerComponents,
                                   databaseRepository: DatabaseRepository,
                                   userRightRepository: UserRightRepository,
                                   UserAction: UserAction,
                                   ConnectionCreatorAction: ConnectionCreatorAction,
                                   ConnectionUserAction: ConnectionUserAction)
  extends AbstractController(cc) {


  def createDatabaseConnection() = UserAction(parse.json[Database]) { request: UserRequest[Database] =>
    val db = request.body
    db.creator = request.user
    db.save()
    Ok(Json.toJson(db))
  }

  def userConnections(createdOnly: Boolean) = UserAction { request: UserRequest[AnyContent] =>
    Ok(Json.toJson(databaseRepository.connectionIdsOfUser(request.user.id, createdOnly)))
  }

  def deleteConnection(id: Long) = UserAction {
    ConnectionCreatorAction(id) { request: ConnectionRequest[AnyContent] =>
      Ok(Json.toJson(databaseRepository.deleteById(id)))
    }
  }

  def updateDatabaseConnectionInfo(id: Long) = UserAction(parse.json) {
    ConnectionCreatorAction(id) { request: ConnectionRequest[JsValue] =>
      val oldDb = request.dbConnection
      request.body.validate(Database.databaseReadsOptionFields(oldDb.dbms)) match {
        case j: JsSuccess[Database] =>
          val newDb = j.get
          var changed = false

          if (newDb.host.nonEmpty && newDb.host != oldDb.host) {
            changed = true
            oldDb.host = newDb.host
          }
          if (newDb.database.nonEmpty && newDb.database != oldDb.database) {
            changed = true
            oldDb.database = newDb.database
          }
          if (newDb.username.nonEmpty && newDb.username != oldDb.username) {
            changed = true
            oldDb.username = newDb.username
          }
          if (newDb.password.nonEmpty && newDb.password != oldDb.password) {
            changed = true
            oldDb.password = newDb.password
          }
          if (newDb.port != oldDb.port) {
            changed = true
            oldDb.port = newDb.port
          }
          if (newDb.dbms != oldDb.dbms) {
            changed = true
            oldDb.dbms = newDb.dbms
          }
          oldDb.save()
          Ok(Json.toJson(oldDb))

        case e: JsError => BadRequest("Failed to parse JSON value")
      }
    }
  }

  def connectionInfo(connectionId: Long) = UserAction { request: UserRequest[AnyContent] =>
    ConnectionUserAction(request, connectionId, SqlRight.READ_ONLY) { connRequest =>
      val rights = userRightRepository.rightsIn(request.user.id, connectionId)
      if (DbUtils.canEditRights(connRequest.dbConnection, request.user.id, rights)) {
        Ok(Json.toJson[Database](connRequest.dbConnection)(Database.databaseExtendedWrites))
      } else {
        Ok(Json.toJson[Database](connRequest.dbConnection))
      }
    }
  }
}
