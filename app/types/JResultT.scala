package types

import play.api.libs.json.Json

case class JResultT(
  var IsSuccess:    Boolean              = false,
  var IsRedirect:   Boolean              = false,
  var RedirectUrl:  String               = "",
  var Notification: Option[Notification] = None)
case class Notification(
  var Title:   String = "Artifidea'dan Bildirim !",
  var Status:  String = "info",
  var Message: String = "")
object Notification {
  implicit val writes = Json.writes[Notification]
  implicit val reads = Json.reads[Notification]
}
object JResultT {
  implicit val writes = Json.writes[JResultT]
  implicit val reads = Json.reads[JResultT]
}