package book

import java.io._
import javax.activation.MimetypesFileTypeMap

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.io.FileUtils
import play.api.Play.current
import play.api.mvc._
import request.PreProcessRequest
import tools.Uncompress

object BookController extends Controller {
  lazy val uploadPath = new File(current.configuration.underlying.getString("upload.path"))
  lazy val fileTypeMap = new MimetypesFileTypeMap()
  lazy val validFileTypes = List(
    "application/x-cbz",
    "application/x-cbt",
    "application/x-cb7"
  )
  lazy val validImageTypes = List("image/jpeg")

  def listGET = PreProcessRequest.authenticatedRequest { implicit req =>
    Ok(book.views.html.list())
  }

  def uploadPOST = PreProcessRequest.authenticatedRequest { implicit req =>

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

              // Remove chars

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

  def coverGET(bookId: Long) = PreProcessRequest(BookStore.isOwner(bookId)) { implicit req =>
    new File(s"${uploadPath.getAbsolutePath}/$bookId/cover").listFiles().headOption map { cover =>
      Ok.sendFile(cover, inline = true)
    } getOrElse {
      BadRequest("File not found...")
    }
  }

  def JSONpagesGET(bookId: Long) = PreProcessRequest(BookStore.isOwner(bookId)) { implicit req =>
    val pageURLs = new File(s"${uploadPath.getAbsolutePath}/$bookId/output").list().map(routes.BookController.pageGET(bookId, _)).mkString("[\"", "\",\"", "\"]")
    Ok(
      s"""
        |{"urls" : $pageURLs}
      """.stripMargin
    )
  }

  def pageGET(bookId: Long, page: String) = PreProcessRequest(BookStore.isOwner(bookId)) { implicit req =>
    val pageFile = new File(s"${uploadPath.getAbsolutePath}/$bookId/output/$page")

    if (pageFile.isFile) {
      Ok.sendFile(pageFile, inline = true)
    } else {
      BadRequest("File not found...")
    }
  }

  def readGET(bookId: Long) = PreProcessRequest(BookStore.isOwner(bookId)) { implicit req =>
    Ok(
      views.html.read(bookId)
    )
  }
}