package transfers

import entities.CategoryET

case class HeaderDTO(
  var Category:      CategoryET      = new CategoryET(0, "", false, 0, None,""),
  var SubCategories: Seq[CategoryET] = Seq())