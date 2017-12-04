package models.entity

import javax.persistence._

import io.ebean.annotation.NotNull


@Entity
@Table
class Database extends BaseModel {
  @NotNull
  @Lob
  var url: String = _

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id")
  var creator: User = _

  @NotNull
  @Enumerated
  var dbms: Dbms = Dbms.PostgreSQL
}