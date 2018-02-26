package websocket

import akka.actor.{Actor, ActorRef, Props}

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class WebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      out ! s"Received message $msg"
  }
}