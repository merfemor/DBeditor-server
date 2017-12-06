package models.repository

import javax.inject.Inject
import javax.persistence.EntityNotFoundException

import io.ebean.Ebean
import models.entity.{Right, UserRight}
import play.db.ebean.EbeanConfig

import scala.collection.JavaConverters._

class UserRightRepository @Inject()(override protected val ebeanConfig: EbeanConfig)
  extends BaseRepository(ebeanConfig: EbeanConfig) {

  def rightsIn(userId: Long, databaseId: Long): Seq[Right] =
    ebeanServer.find(classOf[UserRight])
      .where
      .eq("database_id", databaseId)
      .where
      .eq("user_id", userId)
      .findList
      .asScala
      .map(_.right)


  // TODO: catch exception and make return type Option[String]
  def grantRight(userId: Long, databaseId: Long, right: Right): Unit = {
    val newRight = new UserRight(userId, databaseId)
    newRight.right = right
    Ebean.insert(newRight)
  }


  def findRight(userId: Long, databaseId: Long, right: Right): Option[UserRight] = {
    val userRight = new UserRight(userId, databaseId)
    userRight.right = right
    try {
      userRight.refresh()
      Some(userRight)
    } catch {
      case e: EntityNotFoundException => None
    }
  }
}
