package websocket

import akka.actor.Actor
import play.api.Logger
import websocket.event.AuthorizedSqlQueryEvent

class SqlParseActor extends Actor {
  override def receive = {
    case AuthorizedSqlQueryEvent(query, authInfo) =>
      Logger.debug(logmsg(s"parsing SQL query: $query"))

    /* parse  */

  }

  private def logmsg = s"${WebSocketActor.getClass.getName}:${self.path}: " + _
}
