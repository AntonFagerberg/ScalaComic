package collection

import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import request.PreProcessRequest

object CollectionController extends Controller {
  lazy val validFileTypes = List("application/x-cbz")
  def listGET() = PreProcessRequest.authenticatedRequest { implicit req =>
    Ok(collection.views.html.list())
  }

  def uploadPOST() = PreProcessRequest.authenticatedRequest { implicit req =>
    req.body.asMultipartFormData map { formData =>
      val (valid, invalid) = formData.files.partition(_.contentType.exists(i => validFileTypes.exists(i ==)))
    }

    Ok("")
  }
}