package event.request

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{minLength, _}
import play.api.libs.json.{JsPath, Json, Reads}

object AuthEvent {
  implicit val Reads: Reads[AuthEvent] = (
    (JsPath \ "connection_id").read[Long] and
      (JsPath \ "user_id").read[Long] and
      (JsPath \ "password").read[String](minLength[String](1))
    ) (apply(_, _, _))
}

case class AuthEvent(var connectionId: Long, var userId: Long, var userPassword: String)

object DdlEvent {
  implicit val Reads: Reads[DdlEvent] = Json.reads[DdlEvent]
}

sealed case class DdlEvent(var tableName: String)
