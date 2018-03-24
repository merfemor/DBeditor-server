package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.Materializer
import auth.UserAction
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import websocket.WebSocketActor

import scala.concurrent.Future

@Singleton
class WebSocketController @Inject()(cc: ControllerComponents,
                                    val UserAction: UserAction)
                                   (implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) {

  def optionalSocket: WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful {
      val either = UserAction.invokeCheckError(request).map(user => {
        Logger.info(s"Open WebSocket for user ${user.id}: ${user.username}")
        wsFlow
      })
      if (either.isLeft) {
        Logger.info("Request for WebSocket creation was denied due to the failure of the security check")
        Logger.info("However, it was created!")
        Right(wsFlow)
      } else {
        either
      }
    }
  }

  def socket: WebSocket = WebSocket.accept[String, String] { request =>
    wsFlow
  }

  private def wsFlow = ActorFlow.actorRef(WebSocketActor.props)
}
