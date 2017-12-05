package models.entity

import javax.persistence.{GeneratedValue, Id, MappedSuperclass}

import io.ebean.Model

@MappedSuperclass
protected class BaseModel extends Model {
  @Id
  @GeneratedValue
  var id: Long = _
}