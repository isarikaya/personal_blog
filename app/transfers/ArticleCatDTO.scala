package transfers

import entities.CategoryET
import entities.BlogET
case class ArticleCatDTO(var CategoryName: String = "",
                         var slug: String = "",
                         var Article: BlogET = null,
                         var Description: String = "",
                         var Tag: Seq[String] = Seq())
