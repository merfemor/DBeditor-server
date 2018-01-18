package models.entity

import java.util
import javax.persistence._


import io.ebean.annotation.NotNull
import play.api.libs.json.{JsObject, JsPath, JsResult, JsValue, Json, Reads, Writes}

@Entity
@Table(name = "\"user\"")
class User extends BaseModel {
  @NotNull
  @Lob
  @Column(unique = true)
  var username: String = _

  @NotNull
  @Lob
  @Column(unique = true)
  var email: String = _

  @NotNull
  @Lob
  var password: String = _

  @OneToMany(cascade = Array(CascadeType.ALL), mappedBy = "creator", fetch = FetchType.LAZY)
  var createdDatabases: util.List[Database] = new util.LinkedList[Database]

  @OneToMany(mappedBy = "user", cascade = Array(CascadeType.ALL))
  @PrimaryKeyJoinColumn
  private var userRights: util.List[UserRight] = _

  def this(_username: String, _email: String, _password: String) = {
    this
  }
}

object User {

  def create(username: String, email: String, password: String): User = {
    val u = new User
    u.username = username
    u.email = email
    u.password = password
    u
  }

  import play.api.libs.functional.syntax._
  import play.api.libs.json._
  import play.api.libs.json.Reads._


  implicit val userWrites = new Writes[User] {
    def writes(user: User): JsObject = Json.obj(
      "id" -> user.id,
      "username" -> user.username,
      "email" -> user.email
    )
  }

  implicit val userReads: Reads[User] = (
    (JsPath \ "username").read[String](minLength[String](1)) and
      (JsPath \ "email").read[String](email) and
      (JsPath \ "password").read[String](minLength[String](1))
    ) (create _)

  val userReadsOptionFields: Reads[User] = (
    (JsPath \ "username").readWithDefault[String]("")(minLength[String](1)) and
      (JsPath \ "email").readWithDefault[String]("")(email) and
      (JsPath \ "password").readWithDefault[String]("")(minLength[String](1))
    ) (create _)
}