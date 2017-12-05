package controllers

import javax.inject._

import models.entity.Right
import models.repository._
import play.api.mvc._


@Singleton
class UserRightController @Inject()(cc: ControllerComponents,
                                    userRepository: UserRepository,
                                    userRightsRepository: UserRightRepository)
  extends AbstractController(cc) {

  def grantRight(userId: Long, databaseId: Long) = Action { implicit request: Request[AnyContent] =>
    userRightsRepository.grantRight(userId, databaseId, Right.READ_ONLY)
    Ok("Sample tet")
  }

  def revokeRight(userId: Long, databaseId: Long) = Action { implicit request: Request[AnyContent] =>
    val right = userRightsRepository.findRight(userId, databaseId, Right.READ_ONLY)
    if (right.isDefined) {
      right.get.delete()
      Ok("revoked")
    } else {
      Ok("not found")
    }
  }
}
