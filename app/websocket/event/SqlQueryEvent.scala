package websocket.event

import akka.actor.ActorRef

class SqlQueryEvent(val query: String)

object SqlQueryEvent {

  import play.api.libs.json.Reads._
  import play.api.libs.json._

  implicit val sqlQueryEventReads: Reads[SqlQueryEvent] =
    (JsPath \ "query").read[String].map(new SqlQueryEvent(_))
}

case class AuthorizedSqlQueryEvent(override val query: String, authInfo: AuthInfo, actorRef: ActorRef)
  extends SqlQueryEvent(query = query)