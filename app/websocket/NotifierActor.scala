package websocket

import akka.actor.{Actor, ActorRef, Props}
import websocket.event.{AddUserEvent, NotifyEvent, RemoveUserEvent}

import scala.collection.mutable

object NotifierActor {
  def props = Props(new NotifierActor)
}

class NotifierActor extends Actor {

  private var listeners = mutable.HashMap.empty[Long, mutable.Set[ActorRef]]

  override def receive: PartialFunction[Any, Unit] = {
    case event: NotifyEvent =>
      listeners(event.connectionId).foreach(_ ! event.event)
    case event: AddUserEvent =>
      listeners.getOrElse(event.connectionId, {
        listeners += (event.connectionId -> mutable.Set.empty[ActorRef])
        listeners(event.connectionId)
      }) += event.actor
    case event: RemoveUserEvent =>
      listeners(event.connectionId) -= event.actor
  }
}
