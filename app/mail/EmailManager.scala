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
      case m: ConfirmEmailMessage =>
        Logger.debug(s"Received confirm email message: $m")
      case m: JoinDatabaseNotification =>
        Logger.debug(s"Received join database notification message: $m")
      case _ =>
        Logger.warn("Received a message of unknown type")
    }
  }
}
