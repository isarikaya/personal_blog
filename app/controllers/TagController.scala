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
      def index(slug: String) = Action { implicit request: Request[AnyContent] =>
  val ID = BlogDb.Tags.table
        .filter(x => x.slug === slug)
        .map(x => x.ID)
        .result
        .headOption
        .Save
      if(ID.isDefined) {
        Ok(views.html.tag.index(slug))
      }
      else{
        NotFound("")
      }
  }
  def listByTag(slug: String,counter: Int) = Action {
    implicit request: Request[AnyContent] =>
      val ID = BlogDb.Tags.table
        .filter(x => x.slug === slug)
        .map(x => x.ID)
        .result
        .headOption
        .Save
      if (ID.isDefined) {
        val tagArticles = BlogDb.Blogs.table
          .joinLeft(BlogDb.BlogCategories.table
            .join(BlogDb.Categories.table)
            .on(_.categoryid === _.ID))
          .on(_.ID === _._1.blogid)
          .joinLeft(BlogDb.BlogTags.table
            .join(BlogDb.Tags.table)
            .on(_.tagID === _.ID))
          .on(_._1.ID === _._1.blogID)
          .filter(x => x._2.map(y => y._2.tagName === slug))
          .sortBy(x => x._1._1.date.desc)
          .drop(10 * (counter -1))
          .take(10)
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
                article._1._2.map(c => c._2.categoryName).getOrElse("");
              catSlug = article._1._2.map(a => a._2.slug).getOrElse("");
              Description = outer.substring(0,
                                            if (outer.length >= 150) 150
                                            else outer.length) + "...";
              tagSlug = article._2.map(ts => ts._2.slug).getOrElse("");
              Tag = article._2.map(ts => ts._2.tagName).getOrElse("");
            }
          })
        Ok(Json.toJson(tagArticles))
      } else {
        NotFound("error!")
      }

  }
}
