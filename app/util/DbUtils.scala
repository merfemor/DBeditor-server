package util

import java.sql
import java.sql.DriverManager

import models.entity.Dbms

import scala.util.Try

object DbUtils {
  def JdbcUrl(host: String, port: Int, database: String, dbms: Dbms): String =
    dbms match {
      case Dbms.PostgreSQL => s"jdbc:postgresql://$host" + (if (port > 0) s":$port" else "") + s"/$database"
    }

  def execInStatement[A](url: String, user: String, password: String)(fn: sql.Statement => A): Try[A] = Try {
    val con = DriverManager.getConnection(url, user, password)
    val st = con.createStatement()
    val res = fn(st)
    st.close()
    con.close()
    res
  }
}
