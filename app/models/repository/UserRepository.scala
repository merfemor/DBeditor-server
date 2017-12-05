package models.repository

import java.util
import javax.inject.Inject

import models.entity.{User, UserRight}
import play.db.ebean.EbeanConfig

import scala.collection.JavaConversions._

class UserRepository @Inject()(override protected val ebeanConfig: EbeanConfig) extends BaseRepository[User](ebeanConfig: EbeanConfig) {

  def page(page: Int, pageSize: Int): util.List[User] =
    ebeanServer.find(classOf[User])
      .setFirstRow(page * pageSize)
      .setMaxRows(pageSize)
      .findPagedList
      .getList


  def rightsIn(userId: Long, databaseId: Long) = {
    ebeanServer.find(classOf[UserRight])
      .where()
      .eq("database_id", databaseId)
      .where()
      .eq("user_id", userId)
      .findList()
      .map(userRight => userRight.right)
  }
}
