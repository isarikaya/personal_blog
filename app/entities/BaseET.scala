package entities

import slick.driver.MySQLDriver.api._
import scala.reflect._

abstract class BaseET {
  val ID: Long = 0
}
abstract class BaseTable[E <: BaseET](tag: Tag, Table: String) extends Table[E](tag, Table) {
  def ID: Rep[Long] = column[Long]("ID", O.PrimaryKey, O.AutoInc)
}