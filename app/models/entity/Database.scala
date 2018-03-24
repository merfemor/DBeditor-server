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
  var host: String = _

  @Column(nullable = true)
  var port: Int = _

  @Lob
  @NotNull
  var database: String = _

  @Lob
  @NotNull
  var username: String = _

  @Lob
  @NotNull
  var password: String = _


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

  def create(host: String, port: Int, db: String, user: String, password: String, dbms: Dbms): Database = {
    val d = new Database
    d.host = host
    d.port = port
    d.database = db
    d.username = user
    d.password = password
    d.dbms = dbms
    d
  }

  import play.api.libs.functional.syntax._
  import play.api.libs.json.Reads._
  import play.api.libs.json._

  implicit val dbmsReads: Reads[Dbms] = JsPath.read[String]
    .filter(JsonValidationError("No such database type"))(s =>
      Try.apply[Dbms](Dbms.valueOf(s)).toOption.isDefined
    ).map(Dbms.valueOf)

  implicit val databaseWrites = new Writes[Database] {
    def writes(database: Database): JsObject = Json.obj(
      "id" -> database.id,
      "host" -> database.host,
      "database" -> database.database,
      "port" -> database.port,
      "user" -> database.username,
      "creator_id" -> database.creator.id,
      "dbms" -> database.dbms.toString
    )
  }

  implicit val databaseReads: Reads[Database] = (
    (JsPath \ "host").read[String](minLength[String](1)) and
      (JsPath \ "port").readNullable[Int] and
      (JsPath \ "database").read[String](minLength[String](1)) and
      (JsPath \ "user").read[String](minLength[String](1)) and
      (JsPath \ "password").read[String](minLength[String](1)) and
      (JsPath \ "dbms").read[Dbms]
    ) ((host, port, db, user, pass, dbms) => {
    val nullablePort: Int = if (port.isDefined) port.get else -1
    create(host, nullablePort, db, user, pass, dbms)
  })

  def databaseReadsOptionFields(defaultDbms: Dbms): Reads[Database] = (
    (JsPath \ "host").readWithDefault[String]("") and
      (JsPath \ "port").readNullable[Int] and
      (JsPath \ "database").readWithDefault[String]("") and
      (JsPath \ "user").readWithDefault[String]("") and
      (JsPath \ "password").readWithDefault[String]("") and
      (JsPath \ "dbms").readWithDefault[Dbms](defaultDbms)
    ) ((host, port, db, user, pass, dbms) => {
    val nullablePort: Int = if (port.isDefined) port.get else -1
    create(host, nullablePort, db, user, pass, dbms)
  })
}