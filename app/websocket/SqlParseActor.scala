package websocket

import java.sql.ResultSet

import akka.actor.{Actor, ActorRef, Props}
import controllers.Factory
import models.entity.{Database, SqlRight}
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.insert.Insert
import net.sf.jsqlparser.statement.select.Select
import net.sf.jsqlparser.statement.update.Update
import play.api.Logger
import play.api.libs.json.Json
import util.DbUtils
import websocket.event._
import websocket.model.{Column, DbInfo, Table}

import scala.collection.mutable
import scala.util.Try

object SqlParseActor {
  def props = Props(new SqlParseActor)
}

class SqlParseActor extends Actor {

  import Factory._

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

  private def checkRightsAndExecute(query: String, st: Statement, replyTo: ActorRef, authInfo: AuthInfo): Unit = {
    st match {
      case _: Select =>
        if (!authInfo.rights.exists(SqlRight.isIncludes(SqlRight.READ_ONLY, _))) {
          replyTo ! "Unable to execute SQL query: no right to read"
          return
        }
        executeSelectQuery(query, authInfo).fold(
          e => replyTo ! s"Failed to execute SQL query: ${e.getMessage}",
          replyTo ! Json.toJson(_).toString
        )
      case _: Insert | _: Delete | _: Update =>
        if (!authInfo.rights.exists(SqlRight.isIncludes(SqlRight.DML, _))) {
          replyTo ! "Unable to execute SQL query: no right to modify"
          return
        }
        executeDmlQuery(query, st, authInfo.dbConnection, replyTo)
      // TODO: parse DDL requests
      case _ => replyTo ! "Unsupported SQL query type"
    }
  }

  private def executeSelectQuery(query: String, authInfo: AuthInfo): Try[SelectResponse] = {
    import authInfo.dbConnection._
    val jdbcUrl = DbUtils.jdbcUrl(authInfo.dbConnection)
    DbUtils.execInStatement(jdbcUrl, username, password) { statement =>
      val rs = statement.executeQuery(query)
      val columnsRange = Range(1, rs.getMetaData.getColumnCount + 1)
      var objs = mutable.ArrayBuffer.empty[Array[String]]
      while (rs.next()) {
        objs += columnsRange.map(rs.getString).toArray
      }
      SelectResponse(objs.toArray)
    }
  }

  def executeDmlQuery(query: String, statement: Statement, dbConnection: Database, replyTo: ActorRef): Unit = {
    import dbConnection._
    val url = DbUtils.jdbcUrl(dbConnection)
    Logger.debug(logmsg(s"$url: executing query: $query"))
    DbUtils.execInStatement(url, username, password) { st =>
      st.executeUpdate(query)
    }.fold(
      e => {
        Logger.debug(logmsg(s"$url: failed to execute query: ${e.getMessage}"))
        replyTo ! s"Failed to execute query: ${e.getMessage}"
      },
      _ => {
        val msg = Json.toJson(QueryUpdateEvent(query)).toString
        notifier ! NotifyEvent(msg, id)
      }
    )
  }

  private def receiveDbInfoEvent(event: DbInfoEvent): Unit = {
    import event._
    val url = DbUtils.jdbcUrl(connection)
    Logger.debug(logmsg(s"reading DB info of $url"))
    DbUtils.execInConnection(url, connection.username, connection.password) { connection =>
      val rs: ResultSet = connection.getMetaData.getTables(null, null, "%", Array[String]("TABLE"))
      var tableNames = mutable.ArrayBuffer.empty[String]
      while (rs.next()) {
        tableNames += rs.getString("TABLE_NAME")
      }
      DbInfo(tableNames.map(name => {
        val st = connection.createStatement()
        val rss: ResultSet = st.executeQuery(s"select * from $name;")
        var columns = mutable.ArrayBuffer.empty[Column]
        val md = rss.getMetaData
        for (i <- Range(1, md.getColumnCount + 1)) {
          columns += Column(md.getColumnName(i), md.getColumnTypeName(i))
        }
        st.close()
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
