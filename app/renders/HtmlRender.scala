package renders

import play.twirl.api.Html
import entities.BlogDb
import entities.BlogDb._
import entities._
import slick.driver.MySQLDriver.api._
import scala.collection.mutable.ListBuffer
import transfers.HeaderDTO

object HtmlRender {
  def sideBar(): Html = {
    var storedTags: Seq[String] = Seq()
    val menu = BlogDb.Categories.table.sortBy(x => x.categoryName.desc).take(5).result.Save
    val latest = BlogDb.Blogs.table.sortBy(x => x.date.desc).take(5).result.Save
    val trend = BlogDb.Blogs.table.sortBy(x => x.clickCount.desc).take(5).result.Save
    val blog = BlogDb.Blogs.table.sortBy(x => x.date).result.Save
    val labels = BlogDb.Blogs.table.map(x => x.blogLabel).take(100).result.Save
    val catBlogCount = BlogDb.Categories.table
    .joinLeft(BlogDb.BlogCategories.table)
    .on(_.ID === _.categoryid)
    .filter(p => p._1.parentid.isDefined)
    .groupBy(x=> x._1.categoryName)
    .map(x=> (x._1, x._2.length))
    .sortBy(x=>x._1).take(10).result.Save
    labels.foreach(label => {
      val tags = label.split(",")
      tags.foreach(tag => {
        val trim = tag.trim().toLowerCase()
        if (!storedTags.contains(trim)) {
          storedTags = storedTags :+ trim
        }
      })
    })
    return views.html.layout.partial.navBarr._sideBar(latest, trend, storedTags, catBlogCount)
  }
  def header(): Html = {
    val menu = BlogDb.Categories.table.filter(x => !x.parentid.isDefined)
    .result.Save.map(category => new HeaderDTO {
      Category = category
      SubCategories = BlogDb.Categories.table
        .filter(x=> x.parentid === category.ID).result.Save
    })
    return views.html.layout.partial.navBarr._header(menu)
  }
}
