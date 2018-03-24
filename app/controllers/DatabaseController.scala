package controllers

import javax.inject._

import controllers.auth.{ConnectionCreatorAction, ConnectionCreatorRequest, UserAction, UserRequest}
import models.entity.Database
import models.repository._
import play.api.libs.json._
import play.api.mvc._

import scala.collection.JavaConverters._


@Singleton
class DatabaseController @Inject()(cc: ControllerComponents,
                                   databaseRepository: DatabaseRepository,
                                   UserAction: UserAction,
                                   ConnectionCreatorAction: ConnectionCreatorAction)
  extends AbstractController(cc) {


  def createDatabaseConnection() = UserAction(parse.json[Database]) { request: UserRequest[Database] =>
    val db = request.body
    db.creator = request.user
    db.save()
    Ok(Json.toJson(db))
  }

  def connectionsCreatedByCurrentUser() = UserAction { request: UserRequest[AnyContent] =>
    Ok(Json.toJson(request.user.createdDatabases.asScala))
  }

  def deleteConnection(id: Long) = UserAction {
    ConnectionCreatorAction(id) { request: ConnectionCreatorRequest[AnyContent] =>
      request.dbConnection.delete()
      Ok(Json.toJson(request.dbConnection))
    }
  }

  def updateDatabaseConnectionInfo(id: Long) = UserAction(parse.json) {
    ConnectionCreatorAction(id) { request: ConnectionCreatorRequest[JsValue] =>
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
}
