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

import org.jsoup._

@Singleton
class HomeController @Inject()(cc: ControllerComponents)
    extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
  //println(DateTime.now.getMillis)
    val articleCat: Seq[ArticleCatDTO] = BlogDb.Blogs.table
      .joinLeft(
        BlogDb.BlogCategories.table
          .join(BlogDb.Categories.table)
          .on(_.categoryid === _.ID))
      .on(_.ID === _._1.blogid)
      .sortBy(x => x._1.date.desc)
      .take(15)
      .result
      .Save
      .map(blogCat => {
        val outer = Jsoup
          .parse(blogCat._1.blogArticle)
          .text()
        new ArticleCatDTO {
          Article = blogCat._1;
          CategoryName = blogCat._2.map(y => y._2.categoryName).getOrElse("");
          slug = blogCat._2.map(s => s._2.slug).getOrElse("");
          Description = outer.substring(0,
                                        if (outer.length >= 150) 150
                                        else outer.length) + "...";
          // if(Article.blogArticle.length > 150){
          //   Description = blogCat._1.blogArticle.substring(0,140)+"..."
          // }
          // else{
          //    Description = blogCat._1.blogArticle
          // }
        }
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
      val read = BlogDb.Blogs.table.filter(x => x.ID === ID.get).result.head.Save
      val blogEf = read.copy(clickCount = read.clickCount + 1)
      BlogDb.Blogs.Update(blogEf).Save

      val article = BlogDb.Blogs.table
        .joinLeft(BlogDb.BlogCategories.table)
        .on(_.ID === _.blogid)
        .filter(x => x._2.map(a => a.blogid === ID.get))
        .result
        .headOption
        .Save
        .map(article =>
          new ArticleCatDTO {
            Article = article._1;
            CategoryName = "";
            Tag = article._1.blogLabel.split(",").toSeq
        })

      if (article.isDefined) {
        val labels = article.get.Article.blogLabel.split(",").toSeq
        var relateds: Seq[ArticleCatDTO] = Seq()
        for (label <- labels) {
          if (relateds.length < 3) {
            relateds = relateds ++ BlogDb.Blogs.table
              .filter(x =>
                x.ID =!= article.get.Article.ID &&
                  (x.blogLabel like "%" + label.trim + "%"))
              .take(3 - relateds.length)
              .result
              .Save
              .map(x =>
                new ArticleCatDTO {
                  Article = x
              })
          }
        }

        Ok(views.html.blogDetail.index(article.get, relateds, before, after))
      } else {
        NotFound("not found 404")
      }
    } else {
      NotFound("error!")
    }
  }
}
