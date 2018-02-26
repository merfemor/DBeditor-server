package event.request

case class ConnectionEvent(var connectionId: Long, var userId: Long, var userPassword: String)
