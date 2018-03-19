package forms

import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

case class LoginFormData(ID: Option[Long], username: String, password: String)

object LoginForm { //Post ettiğimiz verileri yakalamak için kullandığımız form.
  val form = Form(
    mapping(
      "ID" -> optional(longNumber),
      "username" -> nonEmptyText,
      "password" -> nonEmptyText)(LoginFormData.apply)(LoginFormData.unapply))
}