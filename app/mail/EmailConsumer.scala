package mail

import javax.inject.Inject

import akka.actor.ActorRef
import com.newmotion.akka.rabbitmq.{ChannelActor, CreateChannel}
import com.rabbitmq.client._
import play.api.Logger

case class EmailConsumer @Inject()(rabbitMQConfig: RabbitMQConfig) {

  import EmailConsumer._
  import rabbitMQConfig._

  private val connection = rabbitMQConfig.connectionActor(ActorName)

  private def setup(channel: Channel, self: ActorRef) {
    val queue = channel.queueDeclare().getQueue
    channel.queueBind(queue, Exchange, "")
    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(a: String, b: Envelope, c: AMQP.BasicProperties, body: Array[Byte]) = {
        val s = new String(body, "UTF-8")
        Logger.info(s"Receive message: $s")
      }
    }
    channel.basicConsume(Queue, true, consumer)
    Logger.info("Setup channel")
  }

  connection ! CreateChannel(ChannelActor.props(setup), Some("consumer"))
}

object EmailConsumer {
  private val ActorName = "email-receiver-connection"
}
