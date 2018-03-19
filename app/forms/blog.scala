package forms

import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

case class BlogFormData(ID: Option[Long], Name: String, Label: String, Content: String,cid: Long)

object BlogForm { //Post ettiğimiz verileri yakalamak için kullandığımız form.
  val form = Form(
    mapping(
      "ID" -> optional(longNumber),
      "Name" -> nonEmptyText,  //.verifying(x=> x.length() > 10 && x.length() < 20) sunucu taraflı şartlarımızı güvenlik nedeniyle buraya yazıyoruz sunucuyu korumak için yapılan bir koruma.,
      "Label" -> nonEmptyText,
      "Content" -> nonEmptyText,
      "cid" -> longNumber)(BlogFormData.apply)(BlogFormData.unapply))
}