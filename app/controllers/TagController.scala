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
class TagController @Inject()(cc: ControllerComponents)
    extends AbstractController(cc) {
  def listByTag(slug: String) = Action {
    implicit request: Request[AnyContent] =>
      val ID = BlogDb.Blogs.table
        .filter(x => x.blogLabel === slug)
        .map(x => x.ID)
        .result
        .headOption
        .Save
      if (ID.isDefined) {
        val tagArticles = BlogDb.Blogs.table
          .joinLeft(
            BlogDb.BlogCategories.table
              .join(BlogDb.Categories.table)
              .on(_.categoryid === _.ID))
          .on(_.ID === _._1.blogid)
          .filter(x => x._1.blogLabel === slug)
          .sortBy(x => x._1.date.desc)
          .take(15)
          .result
          .Save
          .map(article => {
            val outer = Jsoup
              .parse(article._1.blogArticle)
              .text()
            new ArticleCatDTO {
              Article = article._1;
              CategoryName =
                article._2.map(y => y._2.categoryName).getOrElse("")
              slug = article._2.map(y => y._2.slug).getOrElse("")
              Description = outer.substring(0,
                                            if (outer.length >= 150) 150
                                            else outer.length) + "...";
            }
          })
        Ok(views.html.tag.index(tagArticles))
      } else {
        NotFound("error!")
      }

  }
}
