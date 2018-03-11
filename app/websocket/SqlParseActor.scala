package websocket

import java.sql
import java.sql.DriverManager

import akka.actor.{Actor, ActorRef}
import models.entity.SqlRight
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.insert.Insert
import net.sf.jsqlparser.statement.select.Select
import net.sf.jsqlparser.statement.update.Update
import play.api.Logger
import play.api.libs.json.Json
import websocket.event.{AuthInfo, AuthorizedSqlQueryEvent, SelectResponse}

import scala.util.Try

class SqlParseActor extends Actor {
  override def receive: PartialFunction[Any, Unit] = {
    case e: AuthorizedSqlQueryEvent => receiveAuthorizedSqlQueryEvent(e)
    case e =>
      sender ! "Unknown message type"
      Logger.error(logmsg(s"Unknown message type $e"))
  }

  private def receiveAuthorizedSqlQueryEvent(event: AuthorizedSqlQueryEvent): Unit = {
    import event._
    Logger.debug(logmsg(s"parsing SQL query: $query"))
    Try(CCJSqlParserUtil.parse(query)).fold(e => {
      actorRef ! s"Failed to parse SQL query: ${e.getMessage}"
    }, st => {
      checkRightsAndExecute(query, st, actorRef, authInfo)
    })
  }

  private def checkRightsAndExecute(query: String, st: Statement, actorRef: ActorRef, authInfo: AuthInfo): Unit = {
    st match {
      case s: Select =>
        if (!authInfo.rights.exists(SqlRight.isIncludes(SqlRight.READ_ONLY, _))) {
          actorRef ! "Unable to execute SQL query: no right to read"
          return
        }
        executeSelectQuery(query, authInfo).fold(
          e => actorRef ! s"Failed to execute SQL query: ${e.getMessage}",
          resp => actorRef ! Json.toJson(resp)
        )
      case s: Insert | Delete | Update =>
        if (!authInfo.rights.exists(SqlRight.isIncludes(SqlRight.DML, _))) {
          actorRef ! "Unable to execute SQL query: no right to modify"
          return
        }
        ???
      case _ => actorRef ! "Unsupported SQL query type"
    }
  }

  private def executeSelectQuery(query: String, authInfo: AuthInfo): Try[SelectResponse] = {
    execInStatement(authInfo.dbConnection.url) { statement =>
      val rs = statement.executeQuery(query)
      val columnsRange = Range(0, rs.getMetaData.getColumnCount)
      val objs: Seq[Seq[Object]] = Seq[Seq[Object]]()
      while (rs.next()) {
        objs +: columnsRange.map(rs.getObject)
      }
      SelectResponse(objs)
    }
  }

  private def execInStatement[A](url: String)(fn: sql.Statement => A): Try[A] = Try {
    val con = DriverManager.getConnection(url)
    val st = con.createStatement()
    val res = fn(st)
    st.close()
    con.close()
    res
  }

  private def logmsg = s"${WebSocketActor.getClass.getName}:${self.path}: " + _
}
