package models.repository

import javax.inject.Inject

import io.ebean.Expr
import models.entity.{Database, User}
import play.db.ebean.EbeanConfig

import scala.collection.JavaConverters._


class DatabaseRepository @Inject()(override protected val ebeanConfig: EbeanConfig)
  extends IdEntityRepository[Database](ebeanConfig: EbeanConfig) {

  def createdBy(creatorId: Long): Seq[Database] =
    ebeanServer.find(classOf[Database])
      .where
      .eq("creator_id", creatorId)
      .findList
      .asScala


  def managedOrCreatedBy(userId: Long): Seq[Database] =
    ebeanServer.find(classOf[Database])
      .where
      .or(
        Expr.eq("userRights.userRightId.userId", userId),
        Expr.eq("creator_id", userId)
      )
      .findList
      .asScala


  def managedBy(userId: Long): Seq[Database] =
    ebeanServer.find(classOf[Database])
      .where
      .eq("userRights.userRightId.userId", userId)
      .findList
      .asScala


  def usersOf(databaseId: Long): Seq[User] =
    ebeanServer.find(classOf[User])
      .where
      .eq("userRights.userRightId.databaseId", databaseId)
      .findList
      .asScala
}