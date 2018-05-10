package forms

import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

case class QuestionFormData(ID: Option[Long], questionTitle: String, questionContent: String)
object QuestionForm {
  val form = Form(
    mapping(
      "ID" -> optional(longNumber),
      "questionTitle" -> nonEmptyText,  //.verifying(x=> x.length() > 10 && x.length() < 20) sunucu taraflı şartlarımızı güvenlik nedeniyle buraya yazıyoruz sunucuyu korumak için yapılan bir koruma.,
      "questionContent" -> nonEmptyText)(QuestionFormData.apply)(QuestionFormData.unapply))
}