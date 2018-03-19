package compositions

import play.api.mvc._
import play.api.mvc.Results._
import javax.inject.Inject
import scala.concurrent._

class AuthAction @Inject() (parser: BodyParsers.Default)(implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    request.session.get("currentUser").map(x => {
      block(request)
    }).getOrElse({
      Future.successful(Redirect("/admin/account/login"))
    })
  }
}