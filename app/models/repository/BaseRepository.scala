package models.repository

import io.ebean.{Ebean, EbeanServer}
import play.db.ebean.EbeanConfig

protected abstract class BaseRepository(protected val ebeanConfig: EbeanConfig) {
  protected val ebeanServer: EbeanServer = Ebean.getServer(ebeanConfig.defaultServer())
}
