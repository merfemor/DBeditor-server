package websocket

import java.sql.{DriverManager, ResultSet}

import akka.actor.{Actor, ActorRef}
import models.entity.SqlRight
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.insert.Insert
import net.sf.jsqlparser.statement.select.Select
import net.sf.jsqlparser.statement.update.Update
import play.api.Logger
import websocket.event.{AuthInfo, AuthorizedSqlQueryEvent}

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
      checkRights(st, actorRef, authInfo)
      executeStatement(query, st, actorRef, authInfo)
    })
  }

  private def checkRights(st: Statement, actorRef: ActorRef, authInfo: AuthInfo): Unit = {
    st match {
      case s: Select =>
        if (!authInfo.rights.exists(SqlRight.isIncludes(SqlRight.READ_ONLY, _))) {
          actorRef ! "Unable to execute SQL query: no right to read"
          return
        }
      case s: Insert | Delete | Update =>
        if (!authInfo.rights.exists(SqlRight.isIncludes(SqlRight.DML, _))) {
          actorRef ! "Unable to execute SQL query: no right to modify"
          return
        }
      case _ => actorRef ! "Unsupported SQL query type"
    }
  }

  private def executeStatement(query: String, st: Statement, actorRef: ActorRef, authInfo: AuthInfo): Unit = {
    st match {
      case s: Select =>
        executeSelectQuery(query, authInfo).fold(e => {
          actorRef ! s"Failed to execute SQL query: ${e.getMessage}"
        }, rs => {
          ???
        })
      case _ =>
        ???
    }
  }

  private def executeSelectQuery(query: String, authInfo: AuthInfo): Try[ResultSet] = {
    Try {
      val connection = DriverManager.getConnection(authInfo.dbConnection.url)
      val statement = connection.createStatement()
      statement.executeQuery(query)
    }
  }

  private def logmsg = s"${WebSocketActor.getClass.getName}:${self.path}: " + _
}
