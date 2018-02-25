package mail

import javax.inject.{Inject, Singleton}

import play.api.Logger
import queue.{QueueMessageConsumer, QueueMessagePublisher, RabbitMQConfig}

@Singleton
class EmailManager @Inject()(rabbitMQConfig: RabbitMQConfig) {
  private val publisher = QueueMessagePublisher(rabbitMQConfig)
  private val consumer = QueueMessageConsumer(rabbitMQConfig, consume)
  @Inject
  private var emailSender: EmailSender = _

  def send(emailMessage: EmailMessage): Unit = publisher.publish(emailMessage)

  private def consume(o: Object): Unit = {
    o match {
      case m: EmailMessage =>
        emailSender.send(m)
      case _ =>
        Logger.warn("Received a message of unknown type")
    }
  }
}
