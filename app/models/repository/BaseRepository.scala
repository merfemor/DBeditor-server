package models.repository

import io.ebean.{Ebean, EbeanServer}
import play.db.ebean.EbeanConfig

import scala.reflect._

protected abstract class BaseRepository[T: ClassTag](protected val ebeanConfig: EbeanConfig) {
  protected val ebeanServer: EbeanServer = Ebean.getServer(ebeanConfig.defaultServer())

  def findById(id: Long)(implicit ct: ClassTag[T]): T =
    ebeanServer.find(ct.runtimeClass.asInstanceOf[Class[T]], id)
}
