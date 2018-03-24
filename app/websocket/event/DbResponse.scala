package websocket.event

import websocket.model.DbInfo

case class SelectResponse(objs: Seq[Seq[Object]])

case class DbInfoResponse(dbInfo: DbInfo)