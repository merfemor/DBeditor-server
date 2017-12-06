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
    val database = databases.head
    Logger.debug(database.url)

    val memberDatabases = databaseRepository.managedBy(id)
    val memberDatabase = memberDatabases.head
    Logger.debug(memberDatabase.url)

    val memberCDatabases = databaseRepository.managedOrCreatedBy(id)
    val memberCDatabase = memberCDatabases.head
    Logger.debug(memberCDatabase.url)

    Ok(databases.length.toString)
  }


  def usersOfDatabase(databaseId: Long) = Action { implicit request: Request[AnyContent] =>

    val users = databaseRepository.usersOf(databaseId)
    val user = users.head
    Logger.debug(user.username)
    Ok(users.length.toString)
  }


  def deleteDatabase(databaseId: Long) = Action { implicit request: Request[AnyContent] =>
    val database = databaseRepository.findById(databaseId)
    if (database.isDefined) {
      database.get.delete()
      Ok("uraaaa, deleted")
    } else {
      Ok("not found")
    }
  }
}
