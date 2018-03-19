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
import sun.text.normalizer.ICUBinary.Authenticate
import compositions.AuthAction

@Singleton
class LoginController @Inject() (auth: AuthAction, cc: ControllerComponents) extends AbstractController(cc) {
  def login() = Action { implicit request =>
    request.session.get("currentUser").map(x => {
      Redirect("/admin")
    }).getOrElse({
      Ok(admin.views.html.account.login())
    })
  }

  def postLogin() = Action { implicit request: Request[AnyContent] =>
    var notify = new Notification()
    val result = new JResultT(Notification = Some(notify))
    LoginForm.form.bindFromRequest.fold(
      errorForm => {
        notify.Status = "warning"
        notify.Message = "Şu anda bu işlem gerçekleştirilemiyoz"
        BadRequest(Json.toJson(result))
      },
      data => {
        val anyUser = BlogDb.Users.table.filter(x => x.userName === data.username
          && x.Password === data.password).length.result.Save > 0
        if (anyUser) {
          result.IsRedirect = true
          result.RedirectUrl = "/admin"
          notify.Status = "success"
          notify.Message = "Hoşgeldiniz İbrahim Bey iyi çalışmalar dileriz :)"
          Ok(Json.toJson(result)).withSession("currentUser" -> data.username)
        } else {
          notify.Status = "error"
          notify.Message = "Hoooppp Birader Nereye !!!"
          Ok(Json.toJson(result))
        }
      })
  }

  def logout() = auth { implicit request: Request[AnyContent] =>
    Redirect("/admin/account/login").withNewSession
  }
}


  