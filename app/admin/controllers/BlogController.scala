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
import transfers.BlogDTO
import tools._
import com.sksamuel.scrimage.Image
import java.io.File
import com.sksamuel.scrimage.nio.JpegWriter

@Singleton
class BlogController @Inject() (auth: AuthAction, cc: ControllerComponents) extends AbstractController(cc) {
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
        
        var filePath = ""
        request.body.file("picture").map { picture =>
          val filename = Paths.get(picture.filename).getFileName
          val ext = picture.filename.split(Pattern.quote("."))(1)
          val now = DateTime.now
          val name = now.getYear.toString() + now.getMonthOfYear.toString() + now.getDayOfMonth.toString() + now.getHourOfDay.toString() + now.getMinuteOfHour.toString() + now.getMillisOfSecond.toString()
          filePath = s"/pictures/" + "img_" + name
          val nwFileName = filePath + "." + ext
          picture.ref.moveTo(Paths.get(Play.current.path + nwFileName), replace = true)
          implicit val writer = JpegWriter().withCompression(50)
          Image.fromPath(Paths.get(Play.current.path + nwFileName)).scaleToWidth(150).output(new File(Play.current.path + filePath + "_150." + ext))
        }
        val seoUrl = seo seoTitle (data.Name)
        val newBlog = new BlogET(0, data.Name, data.Label, data.Content, filePath, true, DateTime.now.getMillis, 0, seoUrl)
        val res = BlogDb.Blogs.Insert(newBlog).Save
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
      })
  }
  def dropdownList() = auth { implicit request: Request[AnyContent] =>
    implicit val js = Json.format[CategoryET]
    val ddlist = BlogDb.Categories.table.result.Save
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
          .filter(x => (x._1.blogName like "%" + data.search + "%")
            || (x._1.blogLabel like "%" + data.search + "%")
            || (x._2.map(y => y._1._2.categoryName) like "%" + data.search + "%")
            || (x._2.flatMap(y => y._2.map(z => z.categoryName)) like "%" + data.search + "%"))

        //        var action = BlogDb.Blogs.table.filter(x => (x.blogName like "%" + data.search + "%")
        //          || (x.blogLabel like "%" + data.search + "%"))

        data.ordercolumn match {
          case 0 => data.orderdir match {
            case "asc"  => action = action.sortBy(x => x._1.ID.asc)
            case "desc" => action = action.sortBy(x => x._1.ID.desc)
          }
          case 1 => data.orderdir match {
            case "asc"  => action = action.sortBy(x => x._1.blogName.asc)
            case "desc" => action = action.sortBy(x => x._1.blogName.desc)
          }
          case 2 => data.orderdir match {
            case "asc"  => action = action.sortBy(x => x._1.blogLabel.asc)
            case "desc" => action = action.sortBy(x => x._1.blogLabel.desc)
          }
          case 3 => data.orderdir match {
            case "asc"  => action = action.sortBy(x => x._2.flatMap(y => y._2.map(z => z.categoryName)).asc)
            case "desc" => action = action.sortBy(x => x._2.flatMap(y => y._2.map(z => z.categoryName)).desc)
          }
          case 4 => data.orderdir match {
            case "asc"  => action = action.sortBy(x => x._2.map(y => y._1._2.categoryName).asc)
            case "desc" => action = action.sortBy(x => x._2.map(y => y._1._2.categoryName).desc)
          }
          case 5 => data.orderdir match {
            case "asc"  => action = action.sortBy(x => x._1.clickCount.asc)
            case "desc" => action = action.sortBy(x => x._1.clickCount.desc)
          }
          case _ => data.orderdir match {
            case "asc"  => action = action.sortBy(x => x._1.date.asc)
            case "desc" => action = action.sortBy(x => x._1.date.desc)
          }
        }
        sside.recordsTotal = BlogDb.Blogs.table.length.result.Save
        sside.recordsFiltered = action.length.result.Save
        val list = action.drop(data.start).take(data.length).result.Save
          .map(x => new BlogDTO(
            x._1.ID,
            x._1.blogName,
            x._1.blogLabel,
            x._1.date,
            x._1.clickCount,
            x._2.map(y => y._2.map(z => z.categoryName).getOrElse("-")).getOrElse("-"),
            x._2.map(y => y._1._2.categoryName).getOrElse("-")))
        sside.data = Json.toJson(list)
        Ok(Json.toJson(sside))
      })
  }
  //EDİT GET
  def edit(ID: Long) = auth { implicit request =>
    implicit val js = Json.format[BlogET]
    val blog = BlogDb.Blogs.table.filter(x => x.ID === ID).result.headOption.Save
    if (blog.isDefined) {
      Ok(Json.toJson(blog))
    } else {
      NotFound("Error: Kullanıcı Bulunamadı")
    }
  }
  //EDİT POST
  def editt() = auth(parse.multipartFormData) { implicit request =>
    var notify = new Notification()
    val result = new JResultT(Notification = Some(notify))
    BlogForm.form.bindFromRequest.fold(
      errorForm => {
        notify.Status = "error"
        notify.Message = "Lütfen Girdiğiniz Bilgileri Kontrol Edin"
        BadRequest(Json.toJson(result))
      },
      data => { // validaysyon doğru ise buraya girip client ın post ettiği veri ile database deki veri birbirine eşit ise işlem gerçekleşecek.
        var filePath = ""
        request.body.file("picture").map { picture =>
          val filename = Paths.get(picture.filename).getFileName
          val deneme = picture.filename.split(Pattern.quote("."))(1)
          val now = DateTime.now
          val name = now.getYear.toString() + now.getMonthOfYear.toString() + now.getDayOfMonth.toString() + now.getHourOfDay.toString() + now.getMinuteOfHour.toString() + now.getMillisOfSecond.toString()
          filePath = s"/pictures/" + "img_" + name + "." + deneme
          picture.ref.moveTo(Paths.get(Play.current.path + filePath), replace = true)
        }
        val blog = BlogDb.Blogs.table.filter(x => x.ID === data.ID).result.headOption.Save //
        if (blog.isDefined) {
          val entity = blog.get.copy(blogName = data.Name, blogLabel = data.Label, blogArticle = data.Content) // burada ise entity adı altında bir val oluşturup sadece güncellemek istediğimiz elemanları atadık.
          val res = BlogDb.Blogs.Update(entity).Save
          BlogDb.BlogCategories.table.filter(x => x.blogid === blog.get.ID).delete.Save
          result.IsSuccess = true
          BlogDb.BlogCategories.Insert(new BlogCategoryET(0, blog.get.ID, data.cid)).Save
          if (res > 0) {
            notify.Status = "success"
            notify.Message = "Makale Güncellendi"
          } else {
            notify.Status = "warning"
            notify.Message = "Şuan da bu işlem gerçekleştirilemiyor"
          }
        }
        Ok(Json.toJson(result))
      })

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
      notify.Message = "İşlem başarısız oldu"
    }
    Ok(Json.toJson(result))
  }
}
