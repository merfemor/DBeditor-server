package models.repository

import java.util
import javax.inject.Inject

import io.ebean.Expr
import models.entity.Database
import play.db.ebean.EbeanConfig


class DatabaseRepository @Inject()(override protected val ebeanConfig: EbeanConfig) extends BaseRepository[Database](ebeanConfig: EbeanConfig) {

  def createdBy(creatorId: Long): util.List[Database] =
    ebeanServer.find(classOf[Database])
      .where()
      .eq("creator_id", creatorId)
      .findList()

  def managedOrCreatedBy(userId: Long): util.List[Database] =
    ebeanServer.find(classOf[Database])
      .where()
      .or(
        Expr.eq("userRights.userRightId.userId", userId),
        Expr.eq("creator_id", userId)
      ).findList()


  def managedBy(userId: Long): util.List[Database] =
    ebeanServer.find(classOf[Database])
      .where()
      .eq("userRights.userRightId.userId", userId)
      .findList()
}