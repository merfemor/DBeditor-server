package websocket.event

import akka.actor.ActorRef

case class DbEvent()

case class NotifyEvent(event: DbEvent, connectionId: Long)

case class AddUserEvent(actor: ActorRef, connectionId: Long)

case class RemoveUserEvent(actor: ActorRef, connectionId: Long)
