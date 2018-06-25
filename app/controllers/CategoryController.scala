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
  def listByCategory(slug: String) = Action {
    implicit request: Request[AnyContent] =>
      val ID = BlogDb.Categories.table
        .filter(x => x.slug === slug)
        .map(x => x.ID)
        .result
        .headOption
        .Save
      if (ID.isDefined) {
        val articles = BlogDb.Blogs.table
          .joinLeft(BlogDb.BlogCategories.table)
          .on(_.ID === _.blogid)
          .filter(x => x._2.map(y => y.categoryid === ID.get))
          .sortBy(x => x._1.date.desc)
          .take(15)
          .result
          .Save
          .map(article => {
            val outer = Jsoup
              .parse(article._1.blogArticle)
              .text()
            new ArticleCatDTO {
              Article = new BlogDTO {
                blogName = article._1.blogName;
                blogLabel = article._1.blogLabel;
                date = article._1.date;
                clickCount = article._1.clickCount;
                mediumImage = article._1.mediumImage;
                blogUrl = article._1.blogUrl;
              }
              CategoryName = "";
              Description = outer.substring(0,
                                            if (outer.length >= 150) 150
                                            else outer.length) + "...";
            }
          })
        Ok(views.html.category.index(articles))
      } else {
        NotFound("error!")
      }

  }
}
