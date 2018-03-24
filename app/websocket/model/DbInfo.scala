package websocket.model

import play.api.libs.json.Json

object Column {
  implicit val writes = Json.writes[Column]
}

case class Column(name: String, type_name: String)

object Table {
  implicit val writes = Json.writes[Table]
}

case class Table(name: String, columns: Array[Column])

object DbInfo {
  implicit val writes = Json.writes[DbInfo]
}

case class DbInfo(tables: Array[Table])