package transfers

import play.api.libs.json.Json
import entities._

case class BlogDTO(var ID: Long = 0,
                   var blogName: String = "",
                   var blogArticle: String = "",
                   var blogImage: String = "",
                   var thumbImage: String = "",
                   var active: Boolean = false,
                   var date: Long = 0,
                   var dateString: String = "",
                   var clickCount: Long = 0,
                   var blogUrl: String = "",
                   var mediumImage: String = "",
                   var category: String = "",
                   var parentCategory: String = "",
                   var etiket: String = "",
                   var dada : String = "")

object BlogDTO {
  implicit val reads = Json.reads[BlogDTO]
  implicit val writes = Json.writes[BlogDTO]
}
