package entities

import slick.driver.MySQLDriver.api._
import slick.driver.JdbcDriver
import scala.reflect._

case class BlogTagET(
  override val ID: Long,
  val blogID:                  Long,
  val tagID:              Long) extends BaseET
  
  case class BlogTagTable(tag: Tag) extends BaseTable[BlogTagET](tag, "blogTag") {
  val blogID: Rep[Long] = column[Long]("blogID")
  val tagID: Rep[Long] = column[Long]("tagID")
  def * = (ID, blogID, tagID) <> (BlogTagET.tupled, BlogTagET.unapply)
}