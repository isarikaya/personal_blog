//package admin.controllers
//
//import javax.inject._
//import play.api._
//import play.api.mvc._
//import play.api.data
//import play.api.libs.json.Json
//import play.api.libs.json.Writes
//import slick.driver.MySQLDriver.api._
//import entities._
//import BlogDb._
//import forms._
//import java.sql.Connection
//import types._
//
//@Singleton
//
//class UserController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {
//    //POST INSERT
//  def insertPost() = Action { implicit request: Request[AnyContent] =>
//    var notify = new Notification()
//    val result = new JResultT(Notification = Some(notify))
//    UserForm.form.bindFromRequest.fold(
//      errorForm => {
//        notify.Status = "error"
//        notify.Message = "Lütfen girdiğiniz bilgileri kontrol edin."
//        BadRequest(Json.toJson(result))
//      },
//      data => {
//        val newUser = new UserET(0, data.userName, data.Name, data.Surname, data.Password, data.Email,true, DateTime.now.getMillis)
//        val res = BlogDb.Users.insertOrUpdate(newUser).Save
//        if (res > 0) {
//          notify.Status = "success"
//          notify.Message = "Kayıt Başarı İle Gerçekleştirildi"
//        } else {
//          notify.Status = "warning"
//          notify.Message = "Şuan da bu işlem gerçekleştirilemiyor"
//        }
//        Ok(Json.toJson(result))
//      })
//  }
//}
