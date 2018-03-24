package websocket.event

object SelectResponse {
  //  implicit val writes: Writes[SelectResponse] = (
  //    (JsPath \ "objs").write[Seq[Object]]
  //  )(unlift(SelectResponse.unapply))
}

case class SelectResponse(objs: Seq[Seq[Object]])
