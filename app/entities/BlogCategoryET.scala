package entities

import slick.driver.MySQLDriver.api._
import slick.driver.JdbcDriver
import scala.reflect._

case class BlogCategoryET(
  override val ID: Long,
  val blogid:                  Long,
  val categoryid:              Long) extends BaseET
  
  case class BlogCategoryTable(tag: Tag) extends BaseTable[BlogCategoryET](tag, "blogCategory") {
  val blogid: Rep[Long] = column[Long]("blogid")
  val categoryid: Rep[Long] = column[Long]("categoryid")
  def * = (ID, blogid, categoryid) <> (BlogCategoryET.tupled, BlogCategoryET.unapply)
}