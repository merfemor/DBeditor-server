package models.entity

import javax.persistence._

import io.ebean.Model
import io.ebean.annotation.NotNull

@Embeddable
protected class UserRightId {
  var userId: Long = _
  @Column(name = "database_id")
  var databaseId: Long = _

  @NotNull
  @Enumerated
  @Column(name = "user_right")
  var right: Right = _

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

  def right: Right = userRightId.right

  def right_=(r: Right): Unit = userRightId.right = r
}