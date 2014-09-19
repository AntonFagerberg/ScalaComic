package user

import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

object UserController extends Controller {
  lazy val loginForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(LoginDetails.apply)(LoginDetails.unapply).verifying("Username or password is not valid!", loginDetails => {
      UserStore.validate(loginDetails.email, loginDetails.password)
    })
  )

  def loginGET = Action {
    Ok(user.views.html.login(loginForm))
  }

  def loginPOST = Action { implicit req =>
    val sentForm = loginForm.bindFromRequest()

    if (sentForm.hasErrors) {
      BadRequest(user.views.html.login(sentForm))
    } else {
      Redirect(book.routes.BookController.listGET()).withNewSession.withSession("email" -> sentForm.get.email)
    }
  }
}
