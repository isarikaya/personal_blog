package entities

import slick.driver.MySQLDriver.api._
import slick.driver.JdbcDriver
import scala.reflect._

case class CategoryET(
  override val ID:  Long,
  val categoryName: String,
  val active:       Boolean,
  val date:         Long,
  val parentid:     Option[Long]) extends BaseET
  
case class CategoryTable(tag: Tag) extends BaseTable[CategoryET](tag, "Category") {
  val categoryName: Rep[String] = column[String]("categoryName")
  val active: Rep[Boolean] = column[Boolean]("active")
  val date: Rep[Long] = column[Long]("date")
  val parentid: Rep[Option[Long]] = column[Option[Long]]("parentid")

  def * = (ID, categoryName, active, date, parentid) <> (CategoryET.tupled, CategoryET.unapply)
}

  
  
