package mail

import java.net.URL

@SerialVersionUID(1L)
abstract class EmailMessage(email: String, username: String) extends Serializable

@SerialVersionUID(1L)
case class ConfirmEmailMessage(email: String, username: String, confirmCode: String, confirmLink: URL)
  extends EmailMessage(email = email, username = username) with Serializable

@SerialVersionUID(1L)
case class JoinDatabaseNotification(email: String, username: String, creator: String)
  extends EmailMessage(email = email, username = username) with Serializable