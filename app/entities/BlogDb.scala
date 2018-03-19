package entities

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import play.api.Play
import slick.lifted.TableQuery
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag
import slick.driver.MySQLDriver.api._
import patterns.Repository

object BlogDb {
  private val _db = DatabaseConfigProvider.get[JdbcProfile]("BlogDb")(Play.current).db
  
  val Blogs = new Repository[BlogET, BlogTable](TableQuery[BlogTable])
  val Categories =  new Repository[CategoryET, CategoryTable](TableQuery[CategoryTable])
  val BlogCategories =  new Repository[BlogCategoryET, BlogCategoryTable](TableQuery[BlogCategoryTable])
  val Users =new Repository[UserET, UserTable](TableQuery[UserTable])

  //Uzantı metodu olarak çağırabilmemiz için.
  implicit class Runner[R](action: DBIOAction[R, NoStream, Nothing]) {
    def Save = Await.result(_db.run(action), Duration.Inf)
    def SaveAsync = _db.run(action)
  }
}