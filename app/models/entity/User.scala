package models.entity

import java.util
import javax.persistence._

import io.ebean.annotation.NotNull
import play.api.libs.json.{JsObject, JsResult, JsValue, Json, Reads, Writes}

@Entity
@Table(name = "\"user\"")
class User extends BaseModel {
  @NotNull
  @Lob
  var username: String = _

  @NotNull
  @Lob
  var email: String = _

  @NotNull
  @Lob
  var password: String = _

  @OneToMany(cascade = Array(CascadeType.ALL), mappedBy = "creator", fetch = FetchType.LAZY)
  var createdDatabases: util.List[Database] = new util.LinkedList[Database]

  @OneToMany(mappedBy = "user", cascade = Array(CascadeType.ALL))
  @PrimaryKeyJoinColumn
  private var userRights: util.List[UserRight] = _
}

object User {
  implicit val userWrites = new Writes[User] {
    def writes(user: User): JsObject = Json.obj(
      "id" -> user.id,
      "username" -> user.username,
      "email" -> user.email
    )
  }
}