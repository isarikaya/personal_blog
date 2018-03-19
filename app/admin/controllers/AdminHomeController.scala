package admin.controllers

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
import org.joda.time.DateTime
import compositions.AuthAction

@Singleton
class AdminHomeController @Inject() (auth: AuthAction, cc: ControllerComponents) extends AbstractController(cc) {
  def index() = auth { implicit request: Request[AnyContent] =>
    //        var list:Seq[BlogET] = Seq()
    //        for (i <- 1 to 500000) {
    //          BlogDb.Blogs.insertOrUpdate(new BlogET(0, "name_" + i, "label_" + i * 2, "content_" + i * 5 / 4)).Save
    //    }
//    var list: Seq[CategoryET] = Seq()
//    for (i <- 1 to 500) {
//      BlogDb.Categories.insertOrUpdate(new CategoryET(0, "category_Name" + i, true, i * 3 / 2, None)).Save
//    }
    Ok(admin.views.html.home.index())
  }

  def article() = auth { implicit request: Request[AnyContent] =>
    implicit val js = Json.format[BlogET]
    val article = BlogDb.Blogs.table.length.result.Save
    Ok(Json.toJson(article))
  }
}