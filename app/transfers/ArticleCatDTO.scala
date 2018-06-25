package transfers
import play.api.libs.json._
import entities._

case class ArticleCatDTO(var CategoryName: String = "",
                         var slug: String = "",
                         var Article: BlogDTO = null,
                         var Description: String = "",
                         var Tag: Seq[String] = Seq())

      

object ArticleCatDTO {
  implicit val reads = Json.reads[ArticleCatDTO]
  implicit val writes = Json.writes[ArticleCatDTO]
}                         
