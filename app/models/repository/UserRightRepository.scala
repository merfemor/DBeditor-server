package models.repository

import javax.inject.Inject

import models.entity.{Right, UserRight}
import play.db.ebean.EbeanConfig

import scala.collection.JavaConversions._

class UserRightRepository @Inject()(override protected val ebeanConfig: EbeanConfig)
  extends BaseRepository(ebeanConfig: EbeanConfig) {

  def rightsIn(userId: Long, databaseId: Long) =
    ebeanServer.find(classOf[UserRight])
      .where()
      .eq("database_id", databaseId)
      .where()
      .eq("user_id", userId)
      .findList()
      .map(userRight => userRight.right)


  def grantRight(userId: Long, databaseId: Long, right: Right): Unit = {
    val newRight = new UserRight(userId, databaseId)
    newRight.right = right
    ebeanServer.insert(newRight)
  }
}
