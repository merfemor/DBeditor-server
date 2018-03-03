package event.request

case class DmlEvent(var tableName: String) extends DbEvent