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
import transfers.ArticleCatDTO
import tools._

@Singleton
class HomeController @Inject()(cc: ControllerComponents)
    extends AbstractController(cc) {
  def index() = Action { implicit request: Request[AnyContent] =>
    val articleCat: Seq[ArticleCatDTO] = BlogDb.Blogs.table
      .joinLeft(
        BlogDb.BlogCategories.table
          .join(BlogDb.Categories.table)
          .on(_.categoryid === _.ID))
      .on(_.ID === _._1.blogid)
      .sortBy(x => x._1.date.desc).take(15)
      .result
      .Save
      .map(blogCat =>
        new ArticleCatDTO {
          Article = blogCat._1;
          CategoryName = blogCat._2.map(y => y._2.categoryName).getOrElse("");
      })
    Ok(views.html.index(articleCat))
  }
  def insert() = Action { implicit request: Request[AnyContent] =>
    QuestionForm.form.bindFromRequest.fold(
      errorForm => Ok("unsuccess"),
      data => {
        val newQuestion = new QuestionET(0,
                                          data.data(0),
                                          data.data(1),
                                          data.data(2),
                                         DateTime.now.getMillis)
        val res = BlogDb.Questions.Insert(newQuestion).Save
        Ok("success" + res)
      }
    )
  }
}
