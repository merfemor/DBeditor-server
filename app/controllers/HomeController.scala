package controllers

import javax.inject._

import models.entity.Database
import models.repository.{DatabaseRepository, UserRepository}
import play.Logger
import play.api.mvc._

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               userRepository: UserRepository,
                               databaseRepository: DatabaseRepository) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    //Logger.debug(userRepository.list.toString)
    //Logger.debug(databaseRepository.list.get(0).dbms.toString)
    //    var ur = new UserRight()
    //    Logger.debug("right = " + ur.right)
    //    ur.right = models.entity.Right.READ_ONLY
    //    Logger.debug("right = " + ur.right)

    val user = userRepository.list.get(0)

    Logger.debug(user.email)

    val databases = user.databases

    val database = databases.get(0)
    Logger.debug(database.url)
    //println(user.databases == null)
    //Logger.debug()
    val rights = databaseRepository.allUserRights
    val right = rights.get(0)

    Logger.debug(right.right.toString)

    Ok(views.html.main())
  }
}
