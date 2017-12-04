package controllers

import javax.inject._

import models.entity.UserRight
import models.repository.{DatabaseRepository, UserRepository}
import play.Logger
import play.api.mvc._

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               userRepository: UserRepository,
                               databaseRepository: DatabaseRepository) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    Logger.debug(userRepository.list.toString)
    //Logger.debug(databaseRepository.list.get(0).dbms.toString)
    var ur = new UserRight()
    Logger.debug("right = " + ur.right)
    ur.right = models.entity.Right.READ_ONLY
    Logger.debug("right = " + ur.right)
    Ok(views.html.main())
  }
}
