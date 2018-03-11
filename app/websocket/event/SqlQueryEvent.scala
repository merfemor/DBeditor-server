package websocket.event

import akka.actor.ActorRef
import play.api.libs.json.{Json, Reads}

object SqlQueryEvent {
  implicit val reads: Reads[SqlQueryEvent] = Json.reads[SqlQueryEvent]
}

case class SqlQueryEvent(query: String)

case class AuthorizedSqlQueryEvent(override val query: String, authInfo: AuthInfo, actorRef: ActorRef)
  extends SqlQueryEvent(query = query)