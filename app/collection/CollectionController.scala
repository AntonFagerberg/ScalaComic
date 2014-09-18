package collection

import java.io._
import javax.activation.MimetypesFileTypeMap

import book.BookStore
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.io.FileUtils
import play.api.Play.current
import play.api.mvc._
import request.PreProcessRequest
import tools.Uncompress

object CollectionController extends Controller {
  lazy val uploadPath = new File(current.configuration.underlying.getString("upload.path"))
  lazy val fileTypeMap = new MimetypesFileTypeMap()
  lazy val validFileTypes = List(
    "application/x-cbz",
    "application/x-cbt",
    "application/x-cb7"
  )
  lazy val validImageTypes = List("image/jpeg")

  def listGET() = PreProcessRequest.authenticatedRequest { implicit req =>
    Ok(collection.views.html.list())
  }

  def uploadPOST() = PreProcessRequest.authenticatedRequest { implicit req =>

    req.body.asMultipartFormData map { formData =>
      val (validFiles, invalidFiles) = formData.files.partition(_.contentType.exists(i => validFileTypes.exists(i ==)))

      // handle invalid files

      validFiles foreach { file =>
        BookStore.create(file.filename, file.filename, req.user).map { bookId =>

          val bookFolder = new File(s"${uploadPath.getAbsolutePath}/$bookId")

          if (bookFolder.mkdir()) {
            val filePath = new File(s"${bookFolder.getAbsolutePath}/raw")
            val outputFolder = new File(s"${bookFolder.getAbsolutePath}/output")
            val coverFolder = new File(s"${bookFolder.getAbsolutePath}/cover")

            if (outputFolder.mkdir() && coverFolder.mkdir()) {
              file.ref.moveTo(filePath, replace = false)

              // Check for exceptions?
              file.contentType match {
                case Some("application/x-cbz") =>
                  Uncompress[ZipArchiveEntry](filePath.getAbsolutePath, outputFolder.getAbsolutePath)
                case Some("application/x-cbt") =>
                  Uncompress[TarArchiveEntry](filePath.getAbsolutePath, outputFolder.getAbsolutePath)
                case Some("application/x-cb7") =>
                  Uncompress[SevenZArchiveEntry](filePath.getAbsolutePath, outputFolder.getAbsolutePath)
                case _ => // Handle error
              }

              val firstThumbNail = (f: File) => validImageTypes.contains(fileTypeMap.getContentType(f))
              outputFolder.listFiles().find(firstThumbNail).foreach { file =>
                val coverFile = new File(s"$coverFolder/${file.getName}")
                FileUtils.copyFile(file, coverFile)
              }
            } else {
              // Handle error
            }
          } else {
            // Handle error
          }
        }

      }
    }

    Ok("")
  }
}