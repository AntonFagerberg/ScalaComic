package tools

import java.io.{File, FileInputStream, FileOutputStream}

import org.apache.commons.compress.archivers.{ArchiveEntry, ArchiveStreamFactory}
import org.apache.commons.compress.utils.IOUtils

object Uncompress {
  def apply[T <: ArchiveEntry](filePath: String, outputPath: String): Unit = {
    val fileInputStream = new FileInputStream(filePath)
    val archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, fileInputStream)
    var entry = archiveInputStream.getNextEntry.asInstanceOf[T]

    while (entry != null) {
      val fileOutputStream = new FileOutputStream(new File(outputPath, entry.getName))
      IOUtils.copy(archiveInputStream, fileOutputStream)
      fileOutputStream.close()
      entry = archiveInputStream.getNextEntry.asInstanceOf[T]
    }

    archiveInputStream.close()
  }
}