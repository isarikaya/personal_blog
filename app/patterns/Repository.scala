package patterns

import entities._

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import play.api.Play
import slick.lifted.TableQuery
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag
import slick.driver.MySQLDriver.api._

class Repository[E <: BaseET, T <: BaseTable[E]](query: TableQuery[T]) {
  lazy val table: TableQuery[T] = query

  def Insert(entity: E) = {
    (table returning table.map(_.ID)) += entity
  }

  def Update(entity: E) = {
    table.filter(x => x.ID === entity.ID).update(entity)
  }
}