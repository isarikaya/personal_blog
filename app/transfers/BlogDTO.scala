package transfers

import play.api.libs.json.Json

case class BlogDTO(
  val ID:             Long,
  val blogName:       String,
  val blogLabel:      String,
  val date:           Long,
  val clickCount:     Long,
  val category:       String,
  val parentCategory: String)

object BlogDTO {
  implicit val reads = Json.reads[BlogDTO]
  implicit val writes = Json.writes[BlogDTO]
}