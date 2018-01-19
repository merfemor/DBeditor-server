package mail

import akka.actor.{ActorRef, ActorSystem}
import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.Channel
import play.api.Logger

case class EmailPublisher(rabbitMQConfig: RabbitMQConfig) {

  import EmailPublisher._
  import rabbitMQConfig._

  private val connection = rabbitMQConfig.connectionActor(ActorName)
  private val channel = connection.createChannel(ChannelActor.props(setupPublisher), Some("publisher"))
  private val system = ActorSystem()

  def send(emailMessage: EmailMessage) = {
    channel ! ChannelMessage(publish, dropIfNoChannel = false)
  }

  private def publish(channel: Channel) = {
    channel.basicPublish(Exchange, "", null, "Hello, world".getBytes)
    Logger.info("Send basic message")
  }

  def destroy() = {
    system stop connection
  }

  private def setupPublisher(channel: Channel, self: ActorRef) {
    val queue = channel.queueDeclare(Queue, false, false, false, null).getQueue
    channel.queueBind(queue, Exchange, "")
  }
}

object EmailPublisher {
  private val ActorName = "email-publisher-connection"
}

