//package forms
//
//import play.api.Play
//import play.api.data.Form
//import play.api.data.Forms._
//import play.api.db.slick.DatabaseConfigProvider
//import scala.concurrent.Future
//import slick.driver.JdbcProfile
//import slick.driver.MySQLDriver.api._
//import scala.concurrent.ExecutionContext.Implicits.global
//
//case class UserFormData(ID: Option[Long], userName: String, Name: String, Surname: String, Password: String, Email: String)
//
//object UserForm {
//  val form = Form(
//    mapping(
//      "ID" -> optional(longNumber),
//      "userName" -> nonEmptyText,
//      "Name" -> nonEmptyText,
//      "Surname" -> nonEmptyText,
//      "Password" -> nonEmptyText,
//      "Email" -> email)(UserFormData.apply)(UserFormData.unapply))
//}