package user

import play.api._
import play.api.mvc._

object UserController extends Controller {
  def login = Action {
    Ok(user.views.html.login())
  }
}
