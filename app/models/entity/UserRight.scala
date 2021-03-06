package models.entity

import javax.persistence._

import io.ebean.Model
import io.ebean.annotation.NotNull

import scala.util.Try

@Embeddable
protected class UserRightId {
  var userId: Long = _
  @Column(name = "database_id")
  var databaseId: Long = _

  @NotNull
  @Enumerated
  @Column(name = "user_right")
  var right: SqlRight = _

  override def equals(other: Any): Boolean = other match {
    case that: UserRightId =>
      (that canEqual this) &&
        userId == that.userId &&
        databaseId == that.databaseId &&
        right == that.right
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[UserRightId]

  override def hashCode(): Int = {
    val state = Seq(userId, databaseId, right)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

// TODO: maybe set primary key for (user id, connection id)?


@Entity
@Table
class UserRight extends Model {
  @MapsId
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  var user: User = _
  @MapsId
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "database_id", referencedColumnName = "id")
  var database: Database = _

  /**
    * This field is private for the same reason as [[UnverifiedUserInfo.userId]].
    */
  @EmbeddedId
  private var userRightId: UserRightId = new UserRightId

  def this(userId: Long, databaseId: Long) {
    this()
    userRightId.userId = userId
    userRightId.databaseId = databaseId
  }

  def this(userId: Long, databaseId: Long, right: SqlRight) {
    this(userId, databaseId)
    this.right = right
  }

  def right: SqlRight = userRightId.right

  def right_=(r: SqlRight): Unit = userRightId.right = r

  def userId: Long = userRightId.userId

  def databaseId: Long = userRightId.databaseId

}

object UserRight {

  import play.api.libs.functional.syntax._
  import play.api.libs.json.Reads._
  import play.api.libs.json._

  implicit val rightWrites = new Writes[SqlRight] {
    override def writes(o: SqlRight): JsValue = Json.toJson(o.toString)
  }

  implicit val rightReads: Reads[SqlRight] = JsPath.read[String]
    .filter(JsonValidationError("No such right"))(s =>
      Try.apply[SqlRight](SqlRight.valueOf(s)).toOption.isDefined
    ).map(SqlRight.valueOf)

  implicit val userRightWrites = new Writes[UserRight] {
    def writes(userRight: UserRight): JsObject = Json.obj(
      "user_id" -> userRight.userId,
      "database_id" -> userRight.databaseId,
      "right" -> userRight.right.toString
    )
  }

  implicit val userRightReads: Reads[UserRight] = (
    (JsPath \ "user_id").read[Long] and
      (JsPath \ "connection_id").read[Long] and
      (JsPath \ "right").read[SqlRight]
    ) ((u, c, r) => new UserRight(u, c, r))
}