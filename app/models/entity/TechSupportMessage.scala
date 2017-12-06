package models.entity

import javax.persistence._

import io.ebean.annotation.NotNull


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
}
