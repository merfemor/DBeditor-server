package mail

import java.net.URL

@SerialVersionUID(1L)
abstract class EmailMessage(val email: String, val username: String) extends Serializable {
  def content: String

  def subject: String
}

@SerialVersionUID(1L)
case class ConfirmEmailMessage(override val email: String, override val username: String, confirmCode: String, confirmLink: URL)
  extends EmailMessage(email = email, username = username) with Serializable {
  override def content: String =
    s"Hello, $username!\n\n" +
      s"You have successfully registered at DBeditor.\n" +
      s"To confirm your email follow this link:\n" +
      s"${confirmLink.toString + confirmCode}\n\n" +
      s"_______\n" +
      s"DBeditor team"

  override def subject = "Confirm email"
}

@SerialVersionUID(1L)
case class JoinDatabaseNotification(override val email: String, override val username: String, creator: String)
  extends EmailMessage(email = email, username = username) with Serializable {
  override def content: String =
    s"$username!\n\n" +
      s"User $creator have invited you to join his database.\n\n"

  s"_______\n" +
    s"DBeditor team"

  override def subject = "Database invite"
}