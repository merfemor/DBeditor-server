package websocket.event

class SqlQueryEvent(val query: String)

object SqlQueryEvent {

  import play.api.libs.json.Reads._
  import play.api.libs.json._

  implicit val sqlQueryEventReads: Reads[SqlQueryEvent] =
    (JsPath \ "query").read[String].map(new SqlQueryEvent(_))
}