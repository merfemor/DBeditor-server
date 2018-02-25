package mail

@SerialVersionUID(1L)
abstract class EmailMessage(email: String) extends Serializable {
}

@SerialVersionUID(1L)
case class ConfirmEmailMessage(email: String, confirmCode: String)
  extends EmailMessage(email: String) with Serializable {
}