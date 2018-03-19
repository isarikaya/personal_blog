package types

import play.api.libs.json._


case class ServerSide(
  var draw:            Long = 0,
  var recordsTotal:    Long = 0,
  var recordsFiltered: Long = 0,
  var data:            JsValue=JsString(""))
  
object ServerSide {
  implicit val writes = Json.writes[ServerSide]
  implicit val reads = Json.reads[ServerSide]
}