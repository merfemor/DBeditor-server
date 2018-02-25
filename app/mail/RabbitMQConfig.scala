package mail

import akka.actor.{ActorRef, ActorSystem}
import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.ConfigFactory

class RabbitMQConfig {
  private val config = ConfigFactory.load()
  private val factory = new ConnectionFactory()
  private val system = ActorSystem()

  val Host: String = config.getString("rabbitmq.host")
  val Queue: String = config.getString("rabbitmq.queue")
  val Exchange: String = config.getString("rabbitmq.exchange")

  def connectionActor(actorName: String): ActorRef =
    system.actorOf(ConnectionActor.props(factory), actorName)
}
