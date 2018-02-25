package mail

import java.util.Properties
import javax.inject.Singleton
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}

import com.typesafe.config.ConfigFactory
import play.api.Logger

@Singleton
class EmailSender {
  private val config = ConfigFactory.load()
  private val host = config.getString("mail.host")
  private val username = config.getString("mail.username")
  private val password = config.getString("mail.password")
  private val port = config.getString("mail.port")
  private val domain = config.getString("mail.domain")
  private val notifyEmail = config.getString("mail.addresses.notify") + "@" + domain
  private val props: Properties = new Properties()
  private val auth: Authenticator = new Authenticator {
    override def getPasswordAuthentication: PasswordAuthentication = new PasswordAuthentication(username, password)
  }

  props.put("mail.smtp.host", host)
  props.put("mail.smtp.port", port)
  props.put("mail.smtp.auth", "true")
  props.put("mail.smtp.starttls.enable", "true")

  def send(emailMessage: EmailMessage): Unit = {
    val session = Session.getDefaultInstance(props, auth)

    val msg = new MimeMessage(session)
    msg.setFrom(new InternetAddress(notifyEmail))
    msg.setRecipient(Message.RecipientType.TO, new InternetAddress(emailMessage.email))
    msg.setText(emailMessage.content)
    msg.setSubject(emailMessage.subject)

    Transport.send(msg)
    Logger.info(s"Sent email to ${emailMessage.email}")
  }

}
