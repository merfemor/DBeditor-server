package models.repository

import java.util
import javax.inject.Inject

import models.entity.User
import play.db.ebean.EbeanConfig

class UserRepository @Inject()(ebeanConfig: EbeanConfig) extends BaseRepository[User](ebeanConfig: EbeanConfig) {

  def page(page: Int, pageSize: Int): util.List[User] =
    ebeanServer.find(classOf[User])
      .setFirstRow(page * pageSize)
      .setMaxRows(pageSize)
      .findPagedList
      .getList
}
