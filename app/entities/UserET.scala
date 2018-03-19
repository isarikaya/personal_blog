package entities

import slick.driver.MySQLDriver.api._
import slick.driver.JdbcDriver
import scala.reflect._

case class UserET(
  override val ID: Long,
  val userName:    String,
  val Name:        Option[String],
  val Surname:     Option[String],
  val Password:    String,
  val Email:       Option[String],
  val active:      Option[Boolean],
  val date:        Option[Long]) extends BaseET

case class UserTable(tag: Tag) extends BaseTable[UserET](tag, "User") {
  val userName: Rep[String] = column[String]("userName")
  val Name: Rep[Option[String]] = column[Option[String]]("Name")
  val Surname: Rep[Option[String]] = column[Option[String]]("Surname")
  val Password: Rep[String] = column[String]("Password")
  val Email: Rep[Option[String]] = column[Option[String]]("Email")
  val active: Rep[Option[Boolean]] = column[Option[Boolean]]("active")
  val date: Rep[Option[Long]] = column[Option[Long]]("date")

  def * = (ID, userName, Name, Surname, Password, Email, active, date) <> (UserET.tupled, UserET.unapply)

}