package entities

import slick.driver.MySQLDriver.api._
import slick.driver.JdbcDriver
import scala.reflect._

case class BlogET(
  override val ID: Long,
  val blogName:    String,
  val blogLabel:   String,
  val blogArticle: String,
  val blogImage:   String,
  val active:      Boolean,
  val date:        Long,
  val clickCount:  Long,
  val blogUrl: String) extends BaseET

case class BlogTable(tag: Tag) extends BaseTable[BlogET](tag, "Blog") {
  val blogName: Rep[String] = column[String]("blogName")
  val blogLabel: Rep[String] = column[String]("blogLabel")
  val blogArticle: Rep[String] = column[String]("blogArticle")
  val blogImage: Rep[String] = column[String]("blogImage")
  val active: Rep[Boolean] = column[Boolean]("active")
  val date: Rep[Long] = column[Long]("date")
  val clickCount: Rep[Long] = column[Long]("clickCount")
  val blogUrl: Rep[String] = column[String]("blogUrl")
  def * = (ID, blogName, blogLabel, blogArticle, blogImage, active, date, clickCount,blogUrl) <> (BlogET.tupled, BlogET.unapply)

}
