package controllers

import javax.inject._

import models.repository._
import play.Logger
import play.api.mvc._


@Singleton
class DatabaseController @Inject()(cc: ControllerComponents,
                                   databaseRepository: DatabaseRepository)
  extends AbstractController(cc) {


  def databasesOfUser(id: Long) = Action { implicit request: Request[AnyContent] =>
    val databases = databaseRepository.createdBy(id)
    val database = databases.get(0)
    Logger.debug(database.url)

    val memberDatabases = databaseRepository.managedBy(id)
    val memberDatabase = memberDatabases.get(0)
    Logger.debug(memberDatabase.url)

    val memberCDatabases = databaseRepository.managedOrCreatedBy(id)
    val memberCDatabase = memberCDatabases.get(0)
    Logger.debug(memberCDatabase.url)

    Ok(databases.size().toString)
  }
}
