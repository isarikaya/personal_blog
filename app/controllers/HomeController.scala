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

import views.DateExtension._

import org.jsoup._

@Singleton
class HomeController @Inject()(cc: ControllerComponents)
    extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    //println(DateTime.now.getMillis)
    // println(counter)
    //   val articleCat: Seq[ArticleCatDTO] = BlogDb.Blogs.table
    //     .joinLeft(
    //       BlogDb.BlogCategories.table
    //         .join(BlogDb.Categories.table)
    //         .on(_.categoryid === _.ID))
    //     .on(_.ID === _._1.blogid)
    //     .sortBy(x => x._1.date.desc)
    //     .take(15)
    //     .result
    //     .Save
    //     .map(blogCat => {
    //       val outer = Jsoup
    //         .parse(blogCat._1.blogArticle)
    //         .text()
    //       new ArticleCatDTO {
    //         Article = blogCat._1;
    //         CategoryName = blogCat._2.map(y => y._2.categoryName).getOrElse("");
    //         slug = blogCat._2.map(s => s._2.slug).getOrElse("");
    //         Description = outer.substring(0,
    //                                       if (outer.length >= 150) 150
    //                                       else outer.length) + "...";
    //         // if(Article.blogArticle.length > 150){
    //         //   Description = blogCat._1.blogArticle.substring(0,140)+"..."
    //         // }
    //         // else{
    //         //    Description = blogCat._1.blogArticle
    //         // }
    //       }
    //     })
    Ok(views.html.index())
  }
  def list(counter: Int) = Action { implicit request: Request[AnyContent] =>
    val articleCat: Seq[ArticleCatDTO] = BlogDb.Blogs.table
      .joinLeft(
        BlogDb.BlogCategories.table
          .join(BlogDb.Categories.table)
          .on(_.categoryid === _.ID))
      .on(_.ID === _._1.blogid)
      .sortBy(x => x._1.date.desc)
      .drop(10 * (counter - 1))
      .take(10)
      .result
      .Save
      .map(blogCat => {
        val outer = Jsoup
          .parse(blogCat._1.blogArticle)
          .text()
        new ArticleCatDTO {
          Article = new BlogDTO {
            blogName = blogCat._1.blogName;
            date = blogCat._1.date;
            dateString = blogCat._1.date.toDateString;
            blogUrl = blogCat._1.blogUrl;
            clickCount = blogCat._1.clickCount;
            mediumImage = blogCat._1.mediumImage;
            // category = blogCat._2.map(y => y._2.categoryName).getOrElse("");
          }
          CategoryName = blogCat._2.map(y => y._2.categoryName).getOrElse("");
          catSlug = blogCat._2.map(s => s._2.slug).getOrElse("");
          Description = outer.substring(0,
                                        if (outer.length >= 150) 150
                                        else outer.length) + "...";
        }
      })
    Ok(Json.toJson(articleCat))
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
  def detail(url: String) = Action { implicit request: Request[AnyContent] =>
    def rand = () => { SimpleFunction.nullary[Double]("rand") }
    //println(DateTime.now.getMillis)
    val before = BlogDb.Blogs.table.sortBy(x => rand()).result.headOption.Save
    val after = BlogDb.Blogs.table
      .filter(x =>
        (if (before.isDefined) !x.ID.inSetBind(Seq(before.get.ID))
         else x.ID === x.ID))
      .sortBy(x => rand())
      .result
      .headOption
      .Save
    val ID =
      BlogDb.Blogs.table
        .filter(x => x.blogUrl === url)
        .map(x => x.ID)
        .result
        .headOption
        .Save
    if (ID.isDefined) {
      val read =
        BlogDb.Blogs.table.filter(x => x.ID === ID.get).result.head.Save
      val blogEf = read.copy(clickCount = read.clickCount + 1)
      BlogDb.Blogs.Update(blogEf).Save

      val article = BlogDb.Blogs.table
        .joinLeft(BlogDb.BlogCategories.table)
        .on(_.ID === _.blogid)
        .joinLeft(BlogDb.BlogTags.table
          .join(BlogDb.Tags.table)
          .on(_.tagID === _.ID))
        .on(_._1.ID === _._1.blogID)
        .filter(x => x._1._2.map(a => a.blogid === ID.get))
        .result
        .headOption
        .Save
        .map(article =>
          new ArticleCatDTO {
            Article = new BlogDTO {
              blogImage = article._1._1.blogImage;
              blogName = article._1._1.blogName;
              date = article._1._1.date;
              clickCount = article._1._1.clickCount;
              blogArticle = article._1._1.blogArticle;
            };
            CategoryName = "";
        })
      val tag = BlogDb.Blogs.table
        .joinLeft(BlogDb.BlogTags.table)
        .on(_.ID === _.blogID)
        .join(BlogDb.Tags.table)
        .on(_._2.map(x => x.tagID) === _.ID)
        .filter(x => x._1._2.map(a => a.blogID === ID.get))
        .result
        .Save
        .map(articleTag =>
          new ArticleCatDTO {
            Tag = articleTag._2.tagName
            tagSlug = articleTag._2.slug
        })
      if (article.isDefined) {
        val labels = tag.map(z => z.Tag)
        var relateds: Seq[ArticleCatDTO] = Seq()
        for (label <- labels) {
          if (relateds.length < 3) {
            relateds = relateds ++ BlogDb.Blogs.table
              .joinLeft(
                BlogDb.BlogTags.table
                  .join(BlogDb.Tags.table)
                  .on(_.tagID === _.ID))
              .on(_.ID === _._1.blogID)
              .filter(x => x._2.map(y => y._2.tagName like "%" +label+ "%") && x._1.ID =!= ID)
              .take(6 - relateds.length)
              .result
              .Save
              .map(x =>
                new ArticleCatDTO {
                  Article = new BlogDTO {
                    blogName = x._1.blogName;
                    date = x._1.date;
                    blogUrl = x._1.blogUrl;
                    thumbImage = x._1.thumbImage;
                    clickCount = x._1.clickCount;
                  };
              })
          }
        }

        Ok(
          views.html.blogDetail
            .index(article.get, tag, relateds, before, after))
      } else {
        NotFound("not found 404")
      }
    } else {
      NotFound("error!")
    }
  }
  def hakkimda() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.hakkimda())
  }
  def gizlilik() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.gizlilik())
  }
}
