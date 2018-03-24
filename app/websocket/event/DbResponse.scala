package websocket.event

import play.api.libs.json.Json
import websocket.model.DbInfo

object SelectResponse {
  implicit val writes = Json.writes[SelectResponse]
}

case class SelectResponse(objs: Array[Array[String]])

case class DbInfoResponse(dbInfo: DbInfo)