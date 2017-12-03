package models.entity

import javax.persistence.{Entity, Lob, Table}

import io.ebean.annotation.NotNull

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
}
