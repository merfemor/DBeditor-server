package controllers

import javax.inject.{Inject, Singleton}

import models.entity.TechSupportMessage
import models.repository.TechSupportRepository
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Request}
import util.LogUtils

@Singleton
class TechSupportController @Inject()(cc: ControllerComponents,
                                      val techSupportRepository: TechSupportRepository)
  extends AbstractController(cc) {

  def postTechSupportMessage() = Action(parse.json[TechSupportMessage]) { request: Request[TechSupportMessage] =>
    LogUtils.logRequest(request)
    request.body.save()
    Ok(Json.toJson(request.body))
  }
}
