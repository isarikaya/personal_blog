package entities

import slick.driver.MySQLDriver.api._
import slick.driver.JdbcDriver
import scala.reflect._
case class TagET(override val ID: Long,
                 val tagName: String,
                 val slug: String,
                 val date: Long)
    extends BaseET

case class TagTable(tag: Tag) extends BaseTable[TagET](tag, "Tag") {
  val tagName: Rep[String] = column[String]("tagName")
  val slug: Rep[String] = column[String]("slug")
  val date: Rep[Long] = column[Long]("date")
  def * = (ID, tagName, slug, date) <> (TagET.tupled, TagET.unapply)

}
