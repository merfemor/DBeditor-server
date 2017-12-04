package models.entity

import java.util.Date
import javax.persistence._

import io.ebean.Model
import io.ebean.annotation.NotNull


@Entity
@Table
class UnverifiedUserInfo extends Model {
  /**
    * This field is private because it used only for the correct JPA mapping.
    * To access user ID, use user.id instead, which is the same as userId
    */
  @Id
  @Column(name = "user_id")
  private var userId: Long = _
  
  @MapsId
  @OneToOne
  @JoinColumn(name = "user_id")
  var user: User = _

  @NotNull
  @Lob
  var verificationCode: String = _

  @NotNull
  @Temporal(TemporalType.TIMESTAMP)
  var registrationDate: Date = new Date
}