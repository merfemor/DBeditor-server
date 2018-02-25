package mail

@SerialVersionUID(1L)
class ConfirmEmailMessage(override val email: String, val confirmCode: String)
  extends EmailMessage(email: String) with Serializable {
}
