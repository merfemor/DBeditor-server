package websocket

import akka.actor.{Actor, ActorRef, Props}
import com.fasterxml.jackson.core.JsonParseException
import play.api.Logger
import play.api.libs.json.Json

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class WebSocketActor(out: ActorRef) extends Actor {
  Logger.info(logmsg("connection opened"))
  def receive = {
    case msg: String =>
      try {
        val jsValue = Json.parse(msg)
        out ! Json.prettyPrint(jsValue)
      } catch {
        case e: JsonParseException =>
          Logger.error(logmsg(s"failed to parse JSON: ${e.getMessage}"))
          out ! s"failed to parse JSON: ${e.getMessage}"
      }
  }

  override def postStop() = {
    out ! "close connection"
    Logger.info(logmsg("connection closed"))
  }

  private def logmsg = s"${WebSocketActor.getClass.getName}:${self.path}: " + _
}