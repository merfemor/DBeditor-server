package queue

import akka.actor.ActorRef
import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.ConfigFactory
import controllers.Factory

class RabbitMQConfig {
  private val config = ConfigFactory.load()
  private val factory = new ConnectionFactory()

  val Host: String = config.getString("rabbitmq.host")
  val Queue: String = config.getString("rabbitmq.queue")
  val Exchange: String = config.getString("rabbitmq.exchange")

  def connectionActor(actorName: String): ActorRef =
    Factory.actorSystem.actorOf(ConnectionActor.props(factory), actorName)
}
