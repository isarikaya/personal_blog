package transfers

import entities.CategoryET
import entities.BlogET
case class ArticleCatDTO(
        var CategoryName:      String      ="",
        var Article: BlogET = null)
