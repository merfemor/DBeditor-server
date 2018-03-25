package models.repository

import javax.inject.Inject
import javax.persistence.EntityNotFoundException

import models.entity.{SqlRight, UserRight}
import play.db.ebean.EbeanConfig

import scala.collection.JavaConverters._

class UserRightRepository @Inject()(override protected val ebeanConfig: EbeanConfig)
  extends BaseRepository(ebeanConfig: EbeanConfig) {

  def rightsIn(userId: Long, databaseId: Long): Seq[SqlRight] =
    ebeanServer
      .find(classOf[UserRight])
      .where
      .eq("database_id", databaseId)
      .where
      .eq("user_id", userId)
      .findList
      .asScala
      .map(_.right)

  def clearRights(userId: Long, databaseId: Long): Int =
    ebeanServer
      .createUpdate(classOf[UserRight],
        "DELETE UserRight WHERE user_id=:userId and database_id=:databaseId")
      .set("userId", userId)
      .set("databaseId", databaseId)
      .execute()

  @Deprecated
  def findRight(userId: Long, databaseId: Long, right: SqlRight): Option[UserRight] = {
    val userRight = new UserRight(userId, databaseId, right)
    try {
      userRight.refresh()
      Some(userRight)
    } catch {
      case e: EntityNotFoundException => None
    }
  }
}
