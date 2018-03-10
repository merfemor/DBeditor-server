package websocket

import akka.actor.{Actor, ActorRef, Props}
import websocket.event.{AddUserEvent, NotifyEvent, RemoveUserEvent}

object NotifierActor {
  def props = Props(new NotifierActor)
}

class NotifierActor extends Actor {

  private val listeners: Map[Long, Set[ActorRef]] = Map.empty[Long, Set[ActorRef]]

  override def receive = {
    case event: NotifyEvent =>
      listeners(event.connectionId).foreach(_ ! event.event)
    case event: AddUserEvent =>
      listeners.getOrElse(event.connectionId, {
        listeners + (event.connectionId -> Set.empty[ActorRef])
        listeners(event.connectionId)
      }) + event.actor
    case event: RemoveUserEvent =>
      listeners(event.connectionId) - event.actor
  }
}
