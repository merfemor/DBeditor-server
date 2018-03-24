package websocket.event

import play.api.libs.json.Json
import websocket.model.DbInfo

case class SelectResponse(objs: Seq[Seq[Object]])

case class DbResponse(dbInfo: DbInfo)

object DbResponse {
  implicit val writes = Json.writes[DbResponse]
}
