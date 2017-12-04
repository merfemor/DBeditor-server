package models.repository

import java.util
import javax.inject.Inject

import io.ebean.{Ebean, EbeanServer}
import models.entity.Database
import play.db.ebean.EbeanConfig

class DatabaseRepository @Inject()(ebeanConfig: EbeanConfig) {
  val ebeanServer: EbeanServer = Ebean.getServer(ebeanConfig.defaultServer())

  def list: util.List[Database] = {
    ebeanServer.find(classOf[Database]).findList()
  }
}
