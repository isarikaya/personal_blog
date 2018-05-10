package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.data
import play.api.libs.json.Json
import play.api.libs.json.Writes
import slick.driver.MySQLDriver.api._
import entities._
import BlogDb._
import forms._
import java.sql.Connection
import types._
import java.nio.file.Paths
import org.joda.time.DateTime
import java.util.regex.Pattern
import compositions.AuthAction
import transfers.BlogDTO
import tools._

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }
  def insert () = Action {implicit request : Request[AnyContent] => 
    QuestionForm.form.bindFromRequest.fold(
        errorForm => Ok("unsuccess"),
      data => {
        val newQuestion = new QuestionET(0, data.questionTitle, data.questionContent, DateTime.now.getMillis )
        val res = BlogDb.Questions.Insert(newQuestion).Save
        Ok("success"+ res) 
      })
  }
}
