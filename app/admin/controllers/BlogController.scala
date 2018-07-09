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
import java.nio.file.Paths
import org.joda.time.DateTime
import java.util.regex.Pattern
import compositions.AuthAction
import transfers._
import tools._
import com.sksamuel.scrimage.Image
import java.io.File
import com.sksamuel.scrimage.nio.JpegWriter

@Singleton
class BlogController @Inject()(auth: AuthAction, cc: ControllerComponents)
    extends AbstractController(cc) {
  //POST INSERT
  def insertPost = auth(parse.multipartFormData) { implicit request =>
    var notify = new Notification()
    var seo = new SeoTools()
    val result = new JResultT(Notification = Some(notify))
    BlogForm.form.bindFromRequest.fold(
      errorForm => {
        notify.Status = "error"
        notify.Message = "Lütfen girdiğiniz bilgileri kontrol edin."
        BadRequest(Json.toJson(result))
      },
      data => {
        val filePath = "/public/pictures/"
        var fileName = ""
        var fileExt = ""
        request.body.file("picture").map {
          picture =>
            val filename = Paths.get(picture.filename).getFileName
            fileExt = "." + picture.filename.split(Pattern.quote("."))(1)
            val now = DateTime.now
            val name = now.getYear.toString() + now.getMonthOfYear
              .toString() + now.getDayOfMonth.toString() + now.getHourOfDay
              .toString() + now.getMinuteOfHour
              .toString() + now.getMillisOfSecond.toString()
            fileName = "img_" + name
            picture.ref.moveTo(
              Paths.get(Play.current.path + filePath + fileName + fileExt),
              replace = true)
            implicit val writer = JpegWriter().withCompression(50)
            Image
              .fromPath(
                Paths.get(Play.current.path + filePath + fileName + fileExt))
              .scaleToWidth(300)
              .output(new File(
                Play.current.path + filePath + fileName + "_300" + fileExt))
            Image
              .fromPath(
                Paths.get(Play.current.path + filePath + fileName + fileExt))
              .scaleToWidth(600)
              .output(new File(
                Play.current.path + filePath + fileName + "_600" + fileExt))
        }
        val seoUrl = seo seoTitle (data.Name)

        val newBlog = new BlogET(
          0,
          data.Name,
          data.Content,
          filePath.replace("public", "assets") + fileName + fileExt,
          filePath.replace("public", "assets") + fileName + "_300" + fileExt,
          true,
          DateTime.now.getMillis,
          0,
          seoUrl,
          filePath.replace("public", "assets") + fileName + "_600" + fileExt
        )
        val res = BlogDb.Blogs.Insert(newBlog).Save
        val label = data.tag.split(",")
        label.foreach(lbl => {
          val slug = seo seoTitle (lbl)
          val newTag = new TagET(0, lbl, slug,DateTime.now.getMillis)
          val addTag = BlogDb.Tags.Insert(newTag).Save
          BlogDb.BlogTags.Insert(new BlogTagET(0, res, addTag)).Save
          if (addTag > 0) {
            notify.Status = "success"
            notify.Message = "Kayıt Başarı İle Gerçekleştirildi"
          } else {
            notify.Status = "warning"
            notify.Message = "Şuan da bu işlem gerçekleştirilemiyor"
          }
        })

        result.IsSuccess = true
        BlogDb.BlogCategories.Insert(new BlogCategoryET(0, res, data.cid)).Save

        if (res > 0) {
          notify.Status = "success"
          notify.Message = "Kayıt Başarı İle Gerçekleştirildi"
        } else {
          notify.Status = "warning"
          notify.Message = "Şuan da bu işlem gerçekleştirilemiyor"
        }
        Ok(Json.toJson(result))
      }
    )
  }
  def dropdownListt() = auth { implicit request: Request[AnyContent] =>
    implicit val js = Json.format[CategoryET]
    val ddlist =
      BlogDb.Categories.table.filter(x => x.parentid.isDefined).result.Save
    Ok(Json.toJson(ddlist))
  }
  //LIST
  def list() = auth { implicit request: Request[AnyContent] =>
    Ok(admin.views.html.blog.list())
  }
  def source = Action { implicit request =>
    implicit val js = Json.format[BlogET]
    val sside = new ServerSide()
    dtssform.form.bindFromRequest.fold(
      errorForm => {
        BadRequest(Json.toJson(sside))
      },
      data => {
        var action = BlogDb.Blogs.table
          .joinLeft(
            BlogDb.BlogCategories.table
              .join(BlogDb.Categories.table)
              .on(_.categoryid === _.ID)
              .joinLeft(BlogDb.Categories.table)
              .on(_._2.parentid === _.ID))
          .on(_.ID === _._1._1.blogid)
          .filter(x =>
            (x._2
              .map(y => y._1._2.categoryName) like "%" + data.search + "%")
              || (x._2
                .flatMap(y => y._2.map(z => z.categoryName)) like "%" + data.search + "%") || (x._1.blogName like "%" + data.search + "%"))
        data.ordercolumn match {
          case 0 =>
            data.orderdir match {
              case "asc"  => action = action.sortBy(x => x._1.ID.asc)
              case "desc" => action = action.sortBy(x => x._1.ID.desc)
            }
          case 1 =>
            data.orderdir match {
              case "asc"  => action = action.sortBy(x => x._1.blogName.asc)
              case "desc" => action = action.sortBy(x => x._1.blogName.desc)
            }
          case 2 =>
            data.orderdir match {
              case "asc" =>
                action = action.sortBy(x =>
                  x._2.flatMap(y => y._2.map(z => z.categoryName)).asc)
              case "desc" =>
                action = action.sortBy(x =>
                  x._2.flatMap(y => y._2.map(z => z.categoryName)).desc)
            }
          case 3 =>
            data.orderdir match {
              case "asc" =>
                action =
                  action.sortBy(x => x._2.map(y => y._1._2.categoryName).asc)
              case "desc" =>
                action =
                  action.sortBy(x => x._2.map(y => y._1._2.categoryName).desc)
            }
          case 4 =>
            data.orderdir match {
              case "asc"  => action = action.sortBy(x => x._1.clickCount.asc)
              case "desc" => action = action.sortBy(x => x._1.clickCount.desc)
            }
          case _ =>
            data.orderdir match {
              case "asc"  => action = action.sortBy(x => x._1.date.asc)
              case "desc" => action = action.sortBy(x => x._1.date.desc)
            }
        }
        sside.recordsTotal = BlogDb.Blogs.table.length.result.Save
        sside.recordsFiltered = action.length.result.Save
        val list = action
          .drop(data.start)
          .take(data.length)
          .result
          .Save
          .map(x =>
            new BlogDTO {
              val denem = x._1.blogName
              ID = x._1.ID;
              blogName = denem.substring(0,if (denem.length >= 30) 30
                                        else denem.length) + "...";
              date = x._1.date;
              clickCount = x._1.clickCount;
              category = x._2
                .map(y => y._2.map(z => z.categoryName).getOrElse("-"))
                .getOrElse("-");
              parentCategory =
                x._2.map(y => y._1._2.categoryName).getOrElse("-");
          })
        sside.data = Json.toJson(list)
        Ok(Json.toJson(sside))
      }
    )
  }
  //EDİT GET
  def edit(ID: Long) = auth { implicit request =>
    val blog = BlogDb.Blogs.table
      .joinLeft(BlogDb.BlogTags.table)
      .on(_.ID === _.blogID)
      .joinLeft(BlogDb.BlogCategories.table
      .join(BlogDb.Categories.table)
      .on(_.categoryid === _.ID)
      .joinLeft(BlogDb.Categories.table)
              .on(_._2.parentid === _.ID))
      .filter(x => x._1._1.ID === ID && x._1._2.map(t => t.blogID) === ID && x._2.map(c => c._1._1.blogid) === ID)
      .result
      .headOption
      .Save
      .map(bigList => {
        var label = ""
        var a =","
        BlogDb.BlogTags.table
          .filter(x => x.blogID === bigList._1._1.ID)
          .join(BlogDb.Tags.table)
          .on(_.tagID === _.ID)
          .result
          .Save
          .map(t => label += a+ t._2.tagName)
        new BlogDTO {
          ID = bigList._1._1.ID;
          blogName = bigList._1._1.blogName;
          blogArticle = bigList._1._1.blogArticle;
          etiket = label;
          parentCategory = bigList._2.map(z => z._1._2.categoryName).getOrElse("")
          CATID =bigList._2.map(y => y._1._2.ID).getOrElse(0.toLong)
        }
      })
    Ok(Json.toJson(blog))
  }
  def editt() = auth(parse.multipartFormData) { implicit request =>
    var notify = new Notification()
    var seo = new SeoTools()
    val result = new JResultT(Notification = Some(notify))
    BlogForm.form.bindFromRequest.fold(
      errorForm => {
        notify.Status = "error"
        notify.Message = "Lütfen Girdiğiniz Bilgileri Kontrol Edin"
        BadRequest(Json.toJson(result))
      },
      data => {
        var filePath = ""
        request.body.file("picture").map {
          picture =>
            val filename = Paths.get(picture.filename).getFileName
            val ext = picture.filename.split(Pattern.quote("."))(1)
            val now = DateTime.now
            val name = now.getYear.toString() + now.getMonthOfYear
              .toString() + now.getDayOfMonth.toString() + now.getHourOfDay
              .toString() + now.getMinuteOfHour
              .toString() + now.getMillisOfSecond.toString()
            filePath = s"/public/pictures/" + "img_" + name
            val nwFileName = filePath + "." + ext
            picture.ref.moveTo(Paths.get(Play.current.path + nwFileName),
                               replace = true)
            implicit val writer = JpegWriter().withCompression(50)
            Image
              .fromPath(Paths.get(Play.current.path + nwFileName))
              .scaleToWidth(300)
              .output(new File(Play.current.path + filePath + "_300." + ext))
            Image
              .fromPath(Paths.get(Play.current.path + nwFileName))
              .scaleToWidth(600)
              .output(new File(Play.current.path + filePath + "_600." + ext))
        }
        val blog = BlogDb.Blogs.table
          .joinLeft(
            BlogDb.BlogTags.table
              .join(BlogDb.Tags.table)
              .on(_.tagID === _.ID))
          .filter(x => x._1.ID === data.ID)
          .result
          .headOption
          .Save
        if (blog.isDefined) {
          val blogList = blog.get._1.copy(
            blogName = data.Name,
            blogArticle = data.Content
          )
          val res = BlogDb.Blogs.Update(blogList).Save
          BlogDb.BlogCategories.table
            .filter(x => x.blogid === blogList.ID)
            .delete
            .Save
            BlogDb.BlogTags.table
              .filter(x => x.blogID === blog.get._1.ID)
              .delete
              .Save
          BlogDb.BlogCategories
            .Insert(new BlogCategoryET(0, blogList.ID, data.cid))
            .Save
          result.IsSuccess = true
          if (res > 0) {
            notify.Status = "success"
            notify.Message = "Makale güncellemesi Başarılı"
          } else {
            notify.Status = "warning"
            notify.Message = "Makale güncellemesi başarısız"
          }
          data.tag.split(",").foreach(lbl => {
            val slug = seo seoTitle (lbl)
            val newTag = new TagET(0, lbl, slug,DateTime.now.getMillis)
            val tres = BlogDb.Tags.Insert(newTag).Save
            BlogDb.BlogTags.Insert(new BlogTagET(0, blog.get._1.ID, tres)).Save
            if (tres > 0) {
              notify.Status = "success"
              notify.Message = "Makale güncellemesi Başarılı"
            } else {
              notify.Status = "warning"
              notify.Message = "Makale güncellemesi başarısız"
            }
          })
        } else {
          notify.Message = "bulunamadı."
        }
        Ok(Json.toJson(result))
      }
    )
  }
  def remove(ID: Long) = auth { implicit request =>
    implicit val js = Json.format[BlogET]
    var notify = new Notification()
    val result = new JResultT(Notification = Some(notify))
    val efected = BlogDb.Blogs.table.filter(x => x.ID === ID).delete.Save
    if (efected > 0) {
      result.IsSuccess = true
      notify.Status = "success"
      notify.Message = "Silme işlemi başarıyla gerçekleşti."
    } else {
      notify.Status = "error"
      notify.Message = "Silme İşlemi Gerçekleştirilemedi."
    }
    Ok(Json.toJson(result))
  }
}
