package websocket

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.fasterxml.jackson.core.JsonParseException
import com.google.inject.Inject
import models.repository.{DatabaseRepository, UserRepository, UserRightRepository}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import websocket.event._

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class WebSocketActor(out: ActorRef) extends Actor {

  @Inject
  var rightRepository: UserRightRepository = _
  @Inject
  var userRepository: UserRepository = _
  @Inject
  var connectionRepository: DatabaseRepository = _

  private val notifier: ActorRef = ActorSystem().actorOf(NotifierActor.props, "notifier-actor")

  Logger.info(logmsg("connection opened"))
  private var authInfo: Option[AuthInfo] = None

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
      //... executing sql
    })
  }

  override def postStop(): Unit = {
    authInfo match {
      case Some(AuthInfo(connection, _)) =>
        notifier ! RemoveUserEvent(out, connection.id)
    }
    Logger.info(logmsg("connection closed"))
  }

  private def receiveAuthEvent(authEvent: AuthEvent): Unit = {
    Logger.debug(logmsg("receive auth websocket.event"))
    userRepository.findById(authEvent.userId) match {
      case Some(_) =>
        connectionRepository.findById(authEvent.connectionId) match {
          case Some(connection) =>
            val rights = rightRepository.rightsIn(authEvent.userId, authEvent.connectionId)
            if (rights.isEmpty) {
              out ! "No rights for this connection"
            } else {
              authInfo = Some(AuthInfo(connection, rights))
              notifier ! AddUserEvent(out, connection.id)
              out ! "Auth OK"
            }
          case None =>
            out ! "No connection with such id"
        }
      case None =>
        out ! "Failed to authorize: no such user"
    }
  }

  private def logmsg = s"${WebSocketActor.getClass.getName}:${self.path}: " + _
}