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

@Singleton
class QuestionController @Inject()(auth: AuthAction, cc: ControllerComponents)
    extends AbstractController(cc) {
  def list() = auth { implicit request: Request[AnyContent] =>
    Ok(admin.views.html.question.list())
  }
  def source = Action { implicit request =>
    implicit val js = Json.format[QuestionET]
    val sside = new ServerSide()
    dtssform.form.bindFromRequest.fold(
      errorForm => {
        BadRequest(Json.toJson(sside))
      },
      data => {
        var questionList = BlogDb.Questions.table.filter(x =>
          (x.questionTitle like "%" + data.search + "%") || (x.questionContent like "%" + data.search + "%") || (x.email like "%" + data.search +"%"))
        data.ordercolumn match {
          case 0 =>
            data.orderdir match {
              case "asc"  => questionList = questionList.sortBy(x => x.ID.asc)
              case "desc" => questionList = questionList.sortBy(x => x.ID.desc)
            }
          case 1 =>
            data.orderdir match {
              case "asc" =>
                questionList = questionList.sortBy(x => x.questionTitle.asc)
              case "desc" =>
                questionList = questionList.sortBy(x => x.questionTitle.desc)
            }
            case 2 =>
            data.orderdir match {
              case "asc" =>
                questionList = questionList.sortBy(x => x.questionContent.asc)
              case "desc" =>
                questionList = questionList.sortBy(x => x.questionContent.desc)
            }
            case 3 =>
            data.orderdir match {
              case "asc" =>
                questionList = questionList.sortBy(x => x.date.asc)
              case "desc" =>
                questionList = questionList.sortBy(x => x.date.desc)
            }
            
          case _ => {}
        }
        sside.recordsTotal = BlogDb.Questions.table.length.result.Save
        sside.recordsFiltered = questionList.length.result.Save
        val list = questionList.drop(data.start).take(data.length).result.Save
        sside.data = Json.toJson(list)
        Ok(Json.toJson(sside))
      }
    )
  }
  def remove(ID: Long) = auth { implicit request =>
    implicit val js = Json.format[QuestionET]
    var notify = new Notification()
    val result = new JResultT(Notification = Some(notify))
    val efected = BlogDb.Questions.table.filter(x => x.ID === ID).delete.Save
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
