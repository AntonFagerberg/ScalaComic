package collection

import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import request.PreProcessRequest

object CollectionController extends Controller {
  def listGET() = PreProcessRequest.authenticatedRequest { implicit request =>
    Ok(collection.views.html.list())
  }
}
