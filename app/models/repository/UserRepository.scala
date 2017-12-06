package models.repository

import javax.inject.Inject

import models.entity.{UnverifiedUserInfo, User}
import play.db.ebean.EbeanConfig

import scala.collection.JavaConverters._

class UserRepository @Inject()(override protected val ebeanConfig: EbeanConfig)
  extends IdEntityRepository[User](ebeanConfig: EbeanConfig) {

  def all(page: Int, pageSize: Int): Seq[User] =
    ebeanServer.find(classOf[User])
      .setFirstRow(page * pageSize)
      .setMaxRows(pageSize)
      .findPagedList
      .getList
      .asScala

  def search(query: String, page: Int, pageSize: Int): Seq[User] = {
    ebeanServer.find(classOf[User])
      .setFirstRow(page * pageSize)
      .setMaxRows(pageSize)
      .where
      .like("username", query + "%")
      .findPagedList
      .getList
      .asScala
  }

  def getUnverifiedInfo(userId: Long): Option[UnverifiedUserInfo] =
    Option(ebeanServer.find(classOf[UnverifiedUserInfo], userId))


  def getUnverifiedInfo(code: String): Option[UnverifiedUserInfo] =
    Option(
      ebeanServer.find(classOf[UnverifiedUserInfo])
        .where
        .eq("verificationCode", code)
        .findOne
    )
}
