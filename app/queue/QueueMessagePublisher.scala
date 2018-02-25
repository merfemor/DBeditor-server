package queue

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem}
import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.Channel

case class QueueMessagePublisher @Inject()(rabbitMQConfig: RabbitMQConfig) {

  import QueueMessagePublisher._
  import rabbitMQConfig._

  private val connection = rabbitMQConfig.connectionActor(ActorName)
  private val channel = connection.createChannel(ChannelActor.props(setupPublisher), Some("publisher"))
  private val system = ActorSystem()

  def publish(o: Object) = {
    channel ! ChannelMessage(publishBytes(serializeObject(o)), dropIfNoChannel = false)
  }

  private def publishBytes(bytes: Array[Byte])(channel: Channel): Unit = {
    channel.basicPublish(Exchange, "", null, bytes)
  }

  def destroy() = {
    system stop connection
  }

  private def setupPublisher(channel: Channel, self: ActorRef) {
    val queue = channel.queueDeclare(Queue, false, false, false, null).getQueue
    channel.queueBind(queue, Exchange, "")
  }
}

object QueueMessagePublisher {
  private val ActorName = "email-publisher-connection"

  private def serializeObject(o: Object): Array[Byte] = {
    val byteArrayStream = new ByteArrayOutputStream()
    val objectStream = new ObjectOutputStream(byteArrayStream)
    objectStream.writeObject(o)
    objectStream.close()
    val bytes = byteArrayStream.toByteArray
    byteArrayStream.close()
    bytes
  }
}

