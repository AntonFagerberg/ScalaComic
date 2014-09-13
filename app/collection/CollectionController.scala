package collection

import java.io._

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import play.api.Play.current
import play.api.mvc._
import request.PreProcessRequest
import tools.Uncompress

object CollectionController extends Controller {
  lazy val uploadPath = new File(current.configuration.underlying.getString("upload.path"))

  def listGET() = PreProcessRequest.authenticatedRequest { implicit req =>
    Ok(collection.views.html.list())
  }

  def uploadPOST() = PreProcessRequest.authenticatedRequest { implicit req =>
    val validFileTypes = List("application/x-cbz")

    req.body.asMultipartFormData map { formData =>
      val (valid, invalid) = formData.files.partition(_.contentType.exists(i => validFileTypes.exists(i ==)))

      valid foreach { file =>
        val filePath = new File(s"${uploadPath.getAbsolutePath}/${file.filename}")
        file.ref.moveTo(filePath, replace = false)
        // Check filePath != output path ?
        val outputPath = new File(s"${uploadPath.getAbsolutePath}/output")

        if (outputPath.mkdir()) {
          file.contentType match {
            case Some("application/x-cbz") => Uncompress[ZipArchiveEntry](filePath.getAbsolutePath, outputPath.getAbsolutePath)
          }
        }
      }
    }

    Ok("")
  }
}