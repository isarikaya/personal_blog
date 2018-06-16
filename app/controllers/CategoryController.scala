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

@Singleton
class CategoryController @Inject()(cc: ControllerComponents)
    extends AbstractController(cc) {
  def listByCategory(slug: String) = Action { implicit request: Request[AnyContent] =>
  val ID = BlogDb.Categories.table.filter(x => x.slug === slug)
  .map(x => x.ID).result.headOption.Save
  if(ID.isDefined){
    val articles = BlogDb.Blogs.table.joinLeft(BlogDb.BlogCategories.table).on(_.ID === _.blogid)
    .filter(x => x._2.map(y => y.categoryid  === ID.get))
    .result.Save.map(article =>
     new ArticleCatDTO {
      Article = article._1;
      CategoryName = "";
    })
     Ok(views.html.category.index(articles))
  }
  else{
    NotFound("error!")
  }
   
  }
}
