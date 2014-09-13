package request

import play.api.mvc._
import user.{User, UserStore}

object PreProcessRequest extends Controller {
  def authenticatedRequest(result: ProcessedRequest => Result) = PreProcessRequest(UserStore.requestValidation)(result)

  def apply(validation: String => Option[User])(result: ProcessedRequest => Result) = {
    Action { request =>
      request.session.get("email").flatMap(validation).map(user => result(ProcessedRequest(user, request))) getOrElse {
        Redirect(user.routes.UserController.loginGET()).withNewSession
      }
    }
  }
}