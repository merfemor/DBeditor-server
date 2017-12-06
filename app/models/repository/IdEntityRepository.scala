package models.repository

import play.db.ebean.EbeanConfig

import scala.reflect._

protected abstract class IdEntityRepository[T: ClassTag](override protected val ebeanConfig: EbeanConfig)
  extends BaseRepository(ebeanConfig: EbeanConfig) {

  def findById(id: Long)(implicit ct: ClassTag[T]): Option[T] =
    Option(ebeanServer.find(ct.runtimeClass.asInstanceOf[Class[T]], id))
}
