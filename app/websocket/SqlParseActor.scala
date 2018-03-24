package websocket

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
import websocket.event.{AuthInfo, AuthorizedSqlQueryEvent, SelectResponse}

import scala.util.Try

object SqlParseActor {
  def props = Props(new SqlParseActor)
}

class SqlParseActor extends Actor {
  override def receive: PartialFunction[Any, Unit] = {
    case e: AuthorizedSqlQueryEvent => receiveAuthorizedSqlQueryEvent(e)
  }

  private def receiveAuthorizedSqlQueryEvent(event: AuthorizedSqlQueryEvent): Unit = {
    import event._
    Logger.debug(logmsg(s"parsing SQL query: $query"))
    Try(CCJSqlParserUtil.parse(query)).fold(
      e => {
        actorRef ! s"Failed to parse SQL query: ${e.getMessage}"
        Logger.debug(logmsg(s"failed to parse SQL query: ${e.getMessage}"))
      },
      st => checkRightsAndExecute(query, st, actorRef, authInfo))
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

  private def logmsg = s"${WebSocketActor.getClass.getName}:${self.path}: " + _
}
