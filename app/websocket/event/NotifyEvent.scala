package websocket.event

import akka.actor.ActorRef

case class NotifyEvent(message: String, connectionId: Long)

case class AddUserEvent(actor: ActorRef, connectionId: Long)

case class RemoveUserEvent(actor: ActorRef, connectionId: Long)
