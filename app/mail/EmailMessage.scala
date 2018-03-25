package mail

import java.net.URL

import models.entity.{Database, SqlRight}
import util.DbUtils

@SerialVersionUID(1L)
abstract class EmailMessage(val email: String, val username: String) extends Serializable {
  def content: String

  def subject: String
}

@SerialVersionUID(1L)
case class ConfirmEmailMessage(override val email: String, override val username: String, confirmLink: URL)
  extends EmailMessage(email = email, username = username) with Serializable {
  override def content: String =
    s"Hello, $username!\n\n" +
      s"You have successfully registered at DBeditor.\n" +
      s"To confirm your email follow this link:\n" +
      s"$confirmLink\n\n" +
      s"_______\n" +
      s"DBeditor team"

  override def subject = "Confirm email"
}

@SerialVersionUID(1L)
case class ConnectionRightsChangedEmail(override val email: String,
                                        override val username: String,
                                        whoChanged: String,
                                        newRights: Seq[SqlRight],
                                        connection: Database)
  extends EmailMessage(email, username) with Serializable {

  override def content: String =
    s"User $whoChanged have changed your rights in database connection ${DbUtils.jdbcUrl(connection)}.\n" +
      s"Now your rights are: ${newRights.map(_.toString).toSet.mkString(", ")}\n\n" +
      s"_______\n" +
      s"DBeditor team"

  override def subject = "Database rights changed"
}