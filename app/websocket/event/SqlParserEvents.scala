package websocket.event

import akka.actor.ActorRef
import models.entity.Database

case class AuthorizedSqlQueryEvent(query: String, authInfo: AuthInfo, replyTo: ActorRef)

case class DbInfoEvent(connection: Database, replyTo: ActorRef)
