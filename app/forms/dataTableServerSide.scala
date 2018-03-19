package forms


import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

case class DTSS(start: Long, length: Long, search: String, ordercolumn: Long, orderdir: String)

object dtssform { //Post ettiğimiz verileri yakalamak için kullandığımız form. Datatablenin bize gönderdikleri
  val form = Form(
    mapping(
      "start" -> longNumber, // sayfalama 
      "length" -> longNumber, // 10-25-50-100
      "search[value]" -> default(text, ""), //arama 
      "order[0][column]" -> longNumber, // dir ve column hangi kolon hangi yönde sıralanacak asc-desc.
      "order[0][dir]" -> text)(DTSS.apply)(DTSS.unapply))
}