package mail

import javax.inject.Inject

import play.api.Logger
import queue.{QueueMessageConsumer, QueueMessagePublisher, RabbitMQConfig}

class EmailManager @Inject()(rabbitMQConfig: RabbitMQConfig) {
  val publisher = QueueMessagePublisher(rabbitMQConfig)
  val consumer = QueueMessageConsumer(rabbitMQConfig, consume)

  def send(emailMessage: EmailMessage): Unit = publisher.publish(emailMessage)

  private def consume(o: Object): Unit = {
    o match {
      case ConfirmEmailMessage(email, confirmCode) =>
        Logger.debug(s"Consume confirm email message with email = $email, confirmCode = $confirmCode")
      case _ =>
        Logger.warn("Received a message of unknown type")
    }
  }
}
