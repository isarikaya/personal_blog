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
import transfers._
import tools._

import org.jsoup._

@Singleton
class CategoryController @Inject()(cc: ControllerComponents)
    extends AbstractController(cc) {
  def index(slug: String) = Action { implicit request: Request[AnyContent] =>
  val ID = BlogDb.Categories.table
        .filter(x => x.slug === slug)
        .map(x => x.ID)
        .result
        .headOption
        .Save
      if(ID.isDefined) {
        Ok(views.html.category.index(slug))
      }
      else{
        NotFound("")
      }
  }
  def listByCategory(slug: String, counter: Int) = Action {
    implicit request: Request[AnyContent] =>
      val ID = BlogDb.Categories.table
        .filter(x => x.slug === slug)
        .map(x => x.ID)
        .result
        .headOption
        .Save
      if (ID.isDefined) {
        val articles = BlogDb.Blogs.table
          .joinLeft(BlogDb.BlogCategories.table
          .join(BlogDb.Categories.table).on(_.categoryid === _.ID))
          .on(_.ID === _._1.blogid)
          .filter(x => x._2.map(y => y._1.categoryid === ID.get))
          .sortBy(x => x._1.date.desc)
          .drop(10 * (counter - 1))
          .take(10)
          .result
          .Save
          .map(article => {
            val outer = Jsoup
              .parse(article._1.blogArticle)
              .text()
            new ArticleCatDTO {
              Article = new BlogDTO {
                blogName = article._1.blogName;
                date = article._1.date;
                clickCount = article._1.clickCount;
                mediumImage = article._1.mediumImage;
                blogUrl = article._1.blogUrl;
              }
              CategoryName = article._2.map(c => c._2.categoryName).getOrElse("")
              catSlug = article._2.map(c => c._2.slug).getOrElse("")
              Description = outer.substring(0,
                                            if (outer.length >= 150) 150
                                            else outer.length) + "...";
            }
          })
        Ok(Json.toJson(articles))
      } else {
        NotFound("error!")
      }

  }
}
