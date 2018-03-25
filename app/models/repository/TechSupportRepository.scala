package models.repository

import javax.inject.Inject

import models.entity.TechSupportMessage
import play.db.ebean.EbeanConfig

import scala.collection.JavaConverters._

class TechSupportRepository @Inject()(override protected val ebeanConfig: EbeanConfig)
  extends IdEntityRepository[TechSupportMessage](ebeanConfig: EbeanConfig) {

  def all(): Seq[TechSupportMessage] =
    ebeanServer.find(classOf[TechSupportMessage]).findList.asScala
}