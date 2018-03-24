package websocket

import akka.actor.{Actor, ActorRef, Props}
import com.fasterxml.jackson.core.JsonParseException
import models.entity.SqlRight
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import websocket.event._


object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class WebSocketActor(out: ActorRef) extends Actor {

  import controllers.Factory._
  private var authInfo: Option[AuthInfo] = None

  Logger.info(logmsg("connection opened"))
  out ! "Hello! You need to authorize."

  def receive: PartialFunction[Any, Unit] = {
    case msg: String =>
      try {
        val jsValue = Json.parse(msg)
        receiveJsValue(jsValue)
      } catch {
        case e: JsonParseException =>
          Logger.debug(logmsg(s"failed to parse JSON: ${e.getMessage}"))
          out ! s"failed to parse JSON: ${e.getMessage}"
      }
  }

  private def receiveJsValue(jsValue: JsValue): Unit = {
    jsValue.validate[AuthEvent].fold(_ => {
      if (authInfo.isEmpty) {
        Logger.debug(logmsg("reject unauthorized access"))
        out ! "you need to authorize"
        return
      }
      receiveAuthorizedDbEvent(jsValue)
    }, receiveAuthEvent)
  }

  private def receiveAuthorizedDbEvent(jsValue: JsValue): Unit = {
    jsValue.validate[SqlQueryEvent].fold(_ => {
      val msg = "failed to cast json to any known type"
      Logger.debug(logmsg(msg))
      out ! msg
    }, result => {
      Logger.debug(logmsg(s"get SQL query websocket.event: ${result.query}"))
      parser ! AuthorizedSqlQueryEvent(result.query, authInfo.get, out)
    })
  }

  private def logmsg = s"${WebSocketActor.getClass.getName}:${self.path}: " + _

  private def receiveAuthEvent(authEvent: AuthEvent): Unit = {
    Logger.debug(logmsg("receive auth websocket.event"))
    userRepository.findById(authEvent.userId) match {
      case Some(_) =>
        connectionRepository.findById(authEvent.connectionId) match {
          case Some(connection) =>
            var rights = userRightRepository.rightsIn(authEvent.userId, authEvent.connectionId)
            if (authEvent.userId == connection.creator.id) {
              rights = rights :+ SqlRight.DCL
            }
            if (rights.isEmpty) {
              out ! "Failed to authorize: no rights for this connection"
            } else {
              authInfo = Some(AuthInfo(connection, rights))
              notifier ! AddUserEvent(out, connection.id)
              out ! "Auth OK"
              parser ! DbInfoEvent(connection, out)
            }
          case None =>
            out ! "Failed to authorize: no connection with such id"
        }
      case None =>
        out ! "Failed to authorize: no such user"
    }
  }

  override def postStop(): Unit = {
    authInfo.foreach(inf => notifier ! RemoveUserEvent(out, inf.dbConnection.id))
    Logger.info(logmsg("connection closed"))
  }
}