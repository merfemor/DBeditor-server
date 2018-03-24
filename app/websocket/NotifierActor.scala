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
    case e: NotifyEvent =>
      listeners(e.connectionId).foreach(_ ! e.message)
    case e: AddUserEvent =>
      listeners.getOrElse(e.connectionId, {
        listeners += (e.connectionId -> mutable.Set.empty[ActorRef])
        listeners(e.connectionId)
      }) += e.actor
    case e: RemoveUserEvent =>
      listeners(e.connectionId) -= e.actor
  }
}
