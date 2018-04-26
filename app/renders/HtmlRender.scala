package renders

import play.twirl.api.Html
import entities.BlogDb
import entities.BlogDb._
import slick.driver.MySQLDriver.api._

object HtmlRender {
  def sideBar(): Html = {
    val list = BlogDb.Blogs.table.sortBy(x=> x.date).take(5).result.Save
    return views.html.layout.partial.navBarr._sideBar(list)
  }
}