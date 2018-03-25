package models.entity

import javax.persistence.{Entity, Lob, ManyToOne, Table}

import controllers.Factory
import io.ebean.annotation.NotNull

object TechSupportMessage {

  import play.api.libs.functional.syntax._
  import play.api.libs.json.Reads._
  import play.api.libs.json._

  implicit val reads: Reads[TechSupportMessage] = (
    (JsPath \ "message_text").read[String](minLength[String](1)) and
      (JsPath \ "user_id").readNullable[Long] and
      (JsPath \ "email").read[String](email)
    ) ((mess, uid, email) => {
    uid.map { id =>
      val u = Factory.userRepository.findById(id).orNull
      new TechSupportMessage(mess, u, email)
    } getOrElse {
      new TechSupportMessage(mess, null, email)
    }
  })

  implicit val writes = new Writes[TechSupportMessage] {
    def writes(o: TechSupportMessage): JsObject = {
      val obj = Json.obj(
        "id" -> o.id,
        "email" -> o.email,
        "message_text" -> o.messageText
      )
      if (o.user != null) {
        obj + ("user_id" -> Json.toJson(o.user.id))
      } else {
        obj
      }
    }
  }
}


@Entity
@Table
class TechSupportMessage extends BaseModel {
  @NotNull
  @Lob
  var messageText: String = _

  @ManyToOne
  var user: User = _

  @Lob
  var email: String = _

  def this(messageText: String, user: User, email: String) = {
    this()
    this.email = email
    this.messageText = messageText
    this.user = user
  }

  override def toString = s"TechSupportMessage($messageText, ${Option(user).map(_.id).orNull}, $email)"
}
