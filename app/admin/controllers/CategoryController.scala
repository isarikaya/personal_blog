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
import tools._
import org.joda.time.DateTime
import compositions.AuthAction

@Singleton
class CategoryController @Inject()(auth: AuthAction, cc: ControllerComponents)
    extends AbstractController(cc) {

  def insertPost() = auth { implicit request: Request[AnyContent] =>
    var notify = new Notification()
    var seo = new SeoTools()
    val result = new JResultT(Notification = Some(notify))
    CategoryForm.form.bindFromRequest.fold(
      errorForm => {
        notify.Status = "error"
        notify.Message = "Girdiğiniz bilgiler kontrol edin"
        BadRequest(Json.toJson(result))
      },
      data => {
        implicit val js = Json.format[CategoryET]
        val seoUrl = seo seoTitle (data.Name)
        val newCategory = new CategoryET(0,
                                         data.Name,
                                         true,
                                         DateTime.now.getMillis,
                                         data.parentid,seoUrl)
        val deneme = BlogDb.Categories.table
          .filter(x => x.ID === data.parentid)
          .result
          .headOption
          .Save
        if (deneme.isDefined) {
          if (deneme.get.parentid.isDefined) {
            notify.Status = "warning"
            notify.Message =
              "Alt kategoriye yeni bir alt kategori ekleyemezsiniz"
          } else {
            BlogDb.Categories.Insert(newCategory).Save
            notify.Status = "success"
            notify.Message = "Kategori Başarı İle Eklendi"
          }
        } else {
          BlogDb.Categories.Insert(newCategory).Save
          notify.Status = "success"
          notify.Message = "Kategori Başarı İle Eklendi"
        }
        Ok(Json.toJson(result))
      }
    )
  }
  def dropdownList() = auth { implicit request: Request[AnyContent] =>
    implicit val js = Json.format[CategoryET]
    val ddlist = BlogDb.Categories.table.filterNot(x => x.parentid.isDefined).result.Save
    Ok(Json.toJson(ddlist))
  }

  def list() = Action { implicit request: Request[AnyContent] =>
    Ok(admin.views.html.category.list())
  }
  def source = Action { implicit request =>
    implicit val js = Json.format[CategoryET]
    val sside = new ServerSide()
    dtssform.form.bindFromRequest.fold(
      errorForm => {
        BadRequest(Json.toJson(sside))
      },
      data => {
        var action = BlogDb.Categories.table.filter(x =>
          (x.categoryName like "%" + data.search + "%"))

        data.ordercolumn match {
          case 0 =>
            data.orderdir match {
              case "asc"  => action = action.sortBy(x => x.ID.asc)
              case "desc" => action = action.sortBy(x => x.ID.desc)
            }
          case 1 =>
            data.orderdir match {
              case "asc"  => action = action.sortBy(x => x.categoryName.asc)
              case "desc" => action = action.sortBy(x => x.categoryName.desc)
            }
          case _ => {}
        }

        sside.recordsTotal = BlogDb.Categories.table.length.result.Save
        sside.recordsFiltered = action.length.result.Save
        val list = action.drop(data.start).take(data.length).result.Save
        sside.data = Json.toJson(list)
        Ok(Json.toJson(sside))
      }
    )
  }
  def update(ID: Long) = auth { implicit request =>
    implicit val js = Json.format[CategoryET]
    val category =
      BlogDb.Categories.table.filter(x => x.ID === ID).result.headOption.Save
    if (category.isDefined) {
      Ok(Json.toJson(category))
    } else {
      NotFound("Error: Kullanıcı Bulunamadı")
    }
  }
  def updatee() = Action { implicit request =>
    var notify = new Notification()
    var result = new JResultT(Notification = Some(notify))
    CategoryForm.form.bindFromRequest.fold(
      errorForm => {
        notify.Status = "error"
        notify.Message = "Lütfen Girdiğiniz Bilgileri Kontrol Edin"
        BadRequest(Json.toJson(result))
      },
      data => {
        val category = BlogDb.Categories.table
          .filter(x => x.ID === data.ID)
          .result
          .headOption
          .Save
        val entity =
          category.get.copy(categoryName = data.Name, parentid = data.parentid)
        val res = BlogDb.Categories.Update(entity).Save
        if (res > 0) {
          notify.Status = "success"
          notify.Message = "Kategori Güncellendi"

        } else {
          notify.Status = "warning"
          notify.Message = "Şuan da bu işlem gerçekleştirilemiyor"
        }
        Ok(Json.toJson(result))
      }
    )
  }
  def delete(ID: Long) = auth { implicit request =>
    implicit val js = Json.format[CategoryET]
    var notify = new Notification()
    val result = new JResultT(Notification = Some(notify))
    val control =
      BlogDb.Categories.table.filter(x => x.parentid === ID).length.result.Save
    if (control > 0) {
      notify.Status = "error"
      notify.Message =
        "Seçmiş olduğunuz kategorinin  bir yada birden fazla alt kategorileri var lütfen alt kategorileri sildikten sonra tekrar deneyiniz."
    } else {
      val efected = BlogDb.Categories.table.filter(x => x.ID === ID).delete.Save
      if (efected > 0) {
        result.IsSuccess = true
        notify.Status = "success"
        notify.Message = "Talep ettiğiniz işlem başarıyla gerçekleşti."
      }
    }
    //    val efected = BlogDb.Categories.table.filter(x => x.ID === ID).delete.Save
    //    if (efected > 0) {
    //      notify.Status = "success"
    //      notify.Message = "Talep ettiğiniz işlem başarıyla gerçekleşti."
    //    } else {
    //      notify.Status = "error"
    //      notify.Message = "İşlem başarısız oldu"
    //    }
    Ok(Json.toJson(result))
  }

}
