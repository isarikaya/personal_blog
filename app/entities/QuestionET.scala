package entities

import slick.driver.MySQLDriver.api._
import slick.driver.JdbcDriver
import scala.reflect._
case class QuestionET(
  override val ID:     Long,
  val questionTitle:   String,
  val questionContent: String,
  val date:            Long) extends BaseET

case class QuestionTable(tag: Tag) extends BaseTable[QuestionET](tag, "Question") {
  val questionTitle: Rep[String] = column[String]("questionTitle")
  val questionContent: Rep[String] = column[String]("questionContent")
  val date: Rep[Long] = column[Long]("date")
  def * = (ID,questionTitle,questionContent,date) <> (QuestionET.tupled, QuestionET.unapply)
  
}