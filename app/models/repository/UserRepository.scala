package models.repository

import java.util
import javax.inject.Inject

import io.ebean.{Ebean, EbeanServer}
import models.entity.User
import play.db.ebean.EbeanConfig

class UserRepository @Inject()(ebeanConfig: EbeanConfig) {
  val ebeanServer: EbeanServer = Ebean.getServer(ebeanConfig.defaultServer())

  def list: util.List[User] = {
    ebeanServer.find(classOf[User]).findList()
  }
}
