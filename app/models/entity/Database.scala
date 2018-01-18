package models.entity

import java.util
import javax.persistence._

import io.ebean.annotation.NotNull

import scala.util.Try


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

  @OneToMany(mappedBy = "database", cascade = Array(CascadeType.ALL))
  @PrimaryKeyJoinColumn
  private var userRights: util.List[UserRight] = _
}

object Database {

  def create(url: String, dbms: Dbms): Database = {
    val d = new Database
    d.url = url
    d.dbms = dbms
    d
  }

  import play.api.libs.functional.syntax._
  import play.api.libs.json.Reads._
  import play.api.libs.json._

  implicit val dbmsReads: Reads[Dbms] = JsPath.read[String]
    .filter(JsonValidationError("No such database type"))( s =>
      Try.apply[Dbms](Dbms.valueOf(s)).toOption.isDefined
    ).map(Dbms.valueOf)

  implicit val databaseWrites = new Writes[Database] {
    def writes(database: Database): JsObject = Json.obj(
      "id" -> database.id,
      "url" -> database.url,
      "creator_id" -> database.creator.id,
      "dbms" -> database.dbms.toString
    )
  }

  implicit val databaseReads: Reads[Database] = (
    (JsPath \ "url").read[String](minLength[String](1)) and
      (JsPath \ "dbms").read[Dbms]
    ) (create _)

  def databaseReadsOptionFields(defaultDbms: Dbms): Reads[Database] = (
    (JsPath \ "url").readWithDefault[String]("")(minLength[String](1)) and
      (JsPath \ "dbms").readWithDefault[Dbms](defaultDbms)
    ) (create _)
}