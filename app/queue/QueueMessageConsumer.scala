package queue

import java.io.{ByteArrayInputStream, ObjectInputStream}
import javax.inject.Inject

import akka.actor.ActorRef
import com.newmotion.akka.rabbitmq.{ChannelActor, CreateChannel}
import com.rabbitmq.client._

case class QueueMessageConsumer @Inject()(rabbitMQConfig: RabbitMQConfig, handler: Function[Object, Any]) {

  import QueueMessageConsumer._
  import rabbitMQConfig._


  private val connection = rabbitMQConfig.connectionActor(ActorName)

  private def setup(channel: Channel, self: ActorRef) {
    val queue = channel.queueDeclare().getQueue
    channel.queueBind(queue, Exchange, "")
    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(a: String, b: Envelope, c: AMQP.BasicProperties, body: Array[Byte]) = handler(deserializeObject(body))
    }
    channel.basicConsume(Queue, true, consumer)
  }

  connection ! CreateChannel(ChannelActor.props(setup), Some("consumer"))
}

object QueueMessageConsumer {
  private val ActorName = "email-receiver-connection"

  private def deserializeObject(bytes: Array[Byte]): Object = {
    val byteStream = new ByteArrayInputStream(bytes)
    val objectInputStream = new ObjectInputStream(byteStream)
    val o = objectInputStream.readObject()
    objectInputStream.close()
    byteStream.close()
    o
  }
}
