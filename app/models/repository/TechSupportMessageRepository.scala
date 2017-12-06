package models.repository

import javax.inject.Inject

import models.entity.{Database, TechSupportMessage}
import play.db.ebean.EbeanConfig

import scala.collection.JavaConverters._

class TechSupportMessageRepository @Inject()(override protected val ebeanConfig: EbeanConfig)
  extends IdEntityRepository[Database](ebeanConfig: EbeanConfig) {

  def all(): Seq[TechSupportMessage] =
    ebeanServer.find(classOf[TechSupportMessage]).findList.asScala
}
