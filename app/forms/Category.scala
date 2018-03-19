package forms

import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

case class CategoryFormData(ID: Option[Long], Name: String,parentid: Option[Long])

object CategoryForm {
  val form = Form(
    mapping(
      "ID" -> optional(longNumber),
      "Name" -> nonEmptyText,
      "parentid" -> optional(longNumber)
      )(CategoryFormData.apply)(CategoryFormData.unapply))
}