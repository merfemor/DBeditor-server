package models.entity

import javax.persistence._

import io.ebean.Model


@Entity
@Table
class UserProfileInfo extends Model {
  @MapsId
  @OneToOne
  @JoinColumn(name = "user_id")
  var user: User = _
  @Lob
  var firstName: String = _
  @Lob
  var lastName: String = _
  @Lob
  var organizationName: String = _
  /**
    * This field is private for the same reasons as [[UnverifiedUserInfo.userId]].
    * To access user ID, use user.id instead, which is the same as userId
    */
  @Id
  @Column(name = "user_id")
  private var userId: Long = _
}
