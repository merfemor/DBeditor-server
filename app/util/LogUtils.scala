package util

import play.api.Logger
import play.api.mvc.Request

object LogUtils {
  def requestLogMsg[A](request: Request[A]): String = {
    s"${request.method} ${request.path}  ${if (request.hasBody) s" : ${request.body}" else ""}"
  }

  def logRequest[A](request: Request[A]): Unit = Logger.info(requestLogMsg(request))
}
