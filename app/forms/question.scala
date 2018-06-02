package forms

import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

case class QuestionFormData(data:Seq[String])
object QuestionForm {
  val form = Form(
    mapping(
      "data" -> seq(nonEmptyText).verifying(x=> x.length == 3))(QuestionFormData.apply)(QuestionFormData.unapply))
}