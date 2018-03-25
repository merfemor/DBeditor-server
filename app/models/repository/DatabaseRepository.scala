package models.repository

import javax.inject.Inject

import models.entity.{Database, UserRight}
import play.db.ebean.EbeanConfig

import scala.collection.JavaConverters._


class DatabaseRepository @Inject()(override protected val ebeanConfig: EbeanConfig)
  extends IdEntityRepository[Database](ebeanConfig: EbeanConfig) {

  def connectionIdsOfUser(userId: Long, createdOnly: Boolean): Seq[Long] = {
    val q = ebeanServer.find(classOf[Database]).where
    (if (createdOnly) {
      q.eq("creator_id", userId)
    } else {
      q.disjunction()
        .eq("creator_id", userId)
        .eq("userRights.userRightId.userId", userId)
    })
      .findIds[Long]().asScala
  }

  def userIdsOfConnection(connectionId: Long): Set[Long] =
    ebeanServer.find(classOf[UserRight])
      .select("userRightId.userId")
      .setDistinct(true)
      .where
      .eq("userRightId.databaseId", connectionId)
      .findList()
      .asScala
      .map(_.userId)
      .toSet
}