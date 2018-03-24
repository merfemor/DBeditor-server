package websocket.event

import play.api.libs.json.Json

object QueryUpdateEvent {
  implicit val writes = Json.writes[QueryUpdateEvent]
}

case class QueryUpdateEvent(query: String)