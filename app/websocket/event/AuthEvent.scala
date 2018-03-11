package websocket.event

import models.entity.{Database, SqlRight}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.minLength
import play.api.libs.json.{JsPath, Reads}

object AuthEvent {
  implicit val Reads: Reads[AuthEvent] = (
    (JsPath \ "connection_id").read[Long] and
      (JsPath \ "user_id").read[Long] and
      (JsPath \ "password").read[String](minLength[String](1))
    ) (apply(_, _, _))
}

case class AuthEvent(var connectionId: Long, var userId: Long, var userPassword: String)

case class AuthInfo(dbConnection: Database, rights: Seq[SqlRight])