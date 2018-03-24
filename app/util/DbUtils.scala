package util

import models.entity.Dbms

object DbUtils {
  def JdbcUrl(host: String, port: Int, database: String, dbms: Dbms): String =
    dbms match {
      case Dbms.PostgreSQL => s"jdbc:postgresql://$host" + (if (port > 0) s":$port" else "") + s"/$database"
    }
}
