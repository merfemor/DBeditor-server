package util

import java.sql
import java.sql.{Connection, DriverManager}

import models.entity.{Database, Dbms, SqlRight}

import scala.util.Try

object DbUtils {
  def jdbcUrl(host: String, port: Int, database: String, dbms: Dbms): String =
    dbms match {
      case Dbms.PostgreSQL => s"jdbc:postgresql://$host" + (if (port > 0) s":$port" else "") + s"/$database"
    }

  def jdbcUrl(db: Database): String = jdbcUrl(db.host, db.port, db.database, db.dbms)

  def execInStatement[A](url: String, user: String, password: String)(fn: sql.Statement => A): Try[A] = Try {
    val con = DriverManager.getConnection(url, user, password)
    val st = con.createStatement()
    val res = fn(st)
    st.close()
    con.close()
    res
  }

  def execInConnection[A](url: String, user: String, password: String)(fn: Connection => A): Try[A] = Try {
    val con = DriverManager.getConnection(url, user, password)
    val res = fn(con)
    con.close()
    res
  }

  def canEditRights(connection: Database, userId: Long, rights: Seq[SqlRight]): Boolean =
    connection.creator.id == userId || rights.exists(SqlRight.isIncludes(SqlRight.DCL, _))
}
