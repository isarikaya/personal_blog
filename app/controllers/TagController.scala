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
      val ID = BlogDb.Tags.table
        .filter(x => x.slug === slug)
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
          .joinLeft(BlogDb.BlogTags.table
          .join(BlogDb.Tags.table)
          .on(_.tagID === _.ID))
          .on(_._1.ID === _._1.blogID)
          .filter(x => x._2.map(y => y._2.tagName === slug))
          .sortBy(x => x._1._1.date.desc)
          .take(15)
          .result
          .Save
          .map(article => {
            val outer = Jsoup
              .parse(article._1._1.blogArticle)
              .text()
            new ArticleCatDTO {
              Article = new BlogDTO {
                blogName = article._1._1.blogName;
                date = article._1._1.date;
                clickCount = article._1._1.clickCount;
                mediumImage = article._1._1.mediumImage;
                blogUrl = article._1._1.blogUrl;
                //category = article._2.map(y => y._2.categoryName).getOrElse("");
              };
              CategoryName =
                article._1._2.map(c => c._2.categoryName).getOrElse("")
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
