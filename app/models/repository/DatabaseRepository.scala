package models.repository

import java.util
import javax.inject.Inject

import io.ebean.{Ebean, EbeanServer}
import models.entity.{Database, User, UserRight}
import play.db.ebean.EbeanConfig

class DatabaseRepository @Inject()(ebeanConfig: EbeanConfig) {
  val ebeanServer: EbeanServer = Ebean.getServer(ebeanConfig.defaultServer())

  def all: util.List[Database] = {
    ebeanServer.find(classOf[Database]).findList()
  }

  def ofUser(user: User): util.List[Database] = {
    ebeanServer.find(classOf[Database])
      .fetch("user")
      .where().idEq(user.id)
      .findList()
  }

  def allUserRights: util.List[UserRight] = ebeanServer.find(classOf[UserRight]).findList()
}
