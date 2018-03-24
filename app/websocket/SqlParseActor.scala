package websocket

import java.sql.ResultSet

import akka.actor.{Actor, ActorRef, Props}
import models.entity.SqlRight
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.insert.Insert
import net.sf.jsqlparser.statement.select.Select
import net.sf.jsqlparser.statement.update.Update
import play.api.Logger
import play.api.libs.json.Json
import util.DbUtils
import websocket.event.{AuthInfo, AuthorizedSqlQueryEvent, DbInfoEvent, SelectResponse}
import websocket.model.{Column, DbInfo, Table}

import scala.collection.mutable
import scala.util.Try

object SqlParseActor {
  def props = Props(new SqlParseActor)
}

class SqlParseActor extends Actor {
  override def receive: PartialFunction[Any, Unit] = {
    case e: AuthorizedSqlQueryEvent => receiveAuthorizedSqlQueryEvent(e)
    case e: DbInfoEvent => receiveDbInfoEvent(e)
  }

  private def receiveAuthorizedSqlQueryEvent(event: AuthorizedSqlQueryEvent): Unit = {
    import event._
    Logger.debug(logmsg(s"parsing SQL query: $query"))
    Try(CCJSqlParserUtil.parse(query)).fold(
      e => {
        replyTo ! s"Failed to parse SQL query: ${e.getMessage}"
        Logger.debug(logmsg(s"failed to parse SQL query: ${e.getMessage}"))
      },
      st => checkRightsAndExecute(query, st, replyTo, authInfo))
  }

  private def checkRightsAndExecute(query: String, st: Statement, actorRef: ActorRef, authInfo: AuthInfo): Unit = {
    st match {
      case _: Select =>
        if (!authInfo.rights.exists(SqlRight.isIncludes(SqlRight.READ_ONLY, _))) {
          actorRef ! "Unable to execute SQL query: no right to read"
          return
        }
        executeSelectQuery(query, authInfo).fold(
          e => actorRef ! s"Failed to execute SQL query: ${e.getMessage}",
          //resp => actorRef ! Json.toJson(resp)
          resp => actorRef ! Json.toJson("olool")
        )
      case _: Insert | _: Delete | _: Update =>
        if (!authInfo.rights.exists(SqlRight.isIncludes(SqlRight.DML, _))) {
          actorRef ! "Unable to execute SQL query: no right to modify"
          return
        }
        ???
      case _ => actorRef ! "Unsupported SQL query type"
    }
  }


  private def executeSelectQuery(query: String, authInfo: AuthInfo): Try[SelectResponse] = {
    import authInfo.dbConnection._
    val jdbcUrl = DbUtils.JdbcUrl(host, port, database, dbms)
    DbUtils.execInStatement(jdbcUrl, username, password) { statement =>
      val rs = statement.executeQuery(query)
      val columnsRange = Range(0, rs.getMetaData.getColumnCount)
      val objs: Seq[Seq[Object]] = Seq[Seq[Object]]()
      while (rs.next()) {
        objs +: columnsRange.map(rs.getObject)
      }
      SelectResponse(objs)
    }
  }

  private def receiveDbInfoEvent(event: DbInfoEvent) = {
    import event._
    val url = DbUtils.JdbcUrl(connection.host, connection.port, connection.database, connection.dbms)
    Logger.debug(logmsg(s"reading DB info of $url"))
    DbUtils.execInConnection(url, connection.username, connection.password) { connection =>
      val rs: ResultSet = connection.getMetaData.getTables(null, null, "%", Array[String]("TABLE"))
      var tableNames = mutable.ArrayBuffer.empty[String]
      while (rs.next()) {
        tableNames += rs.getString("TABLE_NAME")
      }
      DbInfo(tableNames.map(name => {
        val rss = connection.getMetaData.getColumns(null, null, name, null)
        var columns = mutable.ArrayBuffer.empty[Column]
        while (rss.next()) {
          columns += Column(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"))
        }
        Table(name, columns.toArray)
      }).toArray)
    }.fold(e => {
      Logger.debug(logmsg(s"failed to get db info: ${e.getMessage}"))
      replyTo ! s"Failed to get db info: ${e.getMessage}. Reconnect, please."
    },
      replyTo ! Json.toJson(_).toString)
  }

  private def logmsg = s"${WebSocketActor.getClass.getName}:${self.path}: " + _
}
