package mail

import akka.actor.{ActorRef, ActorSystem}
import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.ConfigFactory

class RabbitMQConfig {
  val Host = config.getString("rabbitmq.host")
  val Queue = config.getString("rabbitmq.queue")
  val Exchange = config.getString("rabbitmq.exchange")
  private val config = ConfigFactory.load()
  private val factory = new ConnectionFactory()
  private val system = ActorSystem()

  def connectionActor(actorName: String): ActorRef =
    system.actorOf(ConnectionActor.props(factory), actorName)
}
