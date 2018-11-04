package com.projectRa.utils

import java.io.File
import dispatch.Req
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import java.io.FileWriter
import scala.io.Source

trait HttpRequestStoreInFileSystem extends HttpRequestStore {
  val baseDir: File

  override def getResponse(req: Req)
                          (implicit ec: ExecutionContext): Future[Option[String]] = Future.successful {
    val cacheFile = new File(baseDir.getAbsolutePath + "/" + req.toPath)
    if (cacheFile.exists())
      Source.fromFile(cacheFile).using { source => Some(source.mkString) }
    else
      None
  }
  
  override def putResponse(req: Req, bodyResponse: String)
                          (implicit ec: ExecutionContext): Future[Boolean] = Future.successful {
    val cacheFile = new File(baseDir.getAbsolutePath + "/" + req.toPath)
    val existsPreviously = cacheFile.exists
    new FileWriter(cacheFile).using { writer =>
      writer.write(bodyResponse)
      existsPreviously
    }
  }
  
  private case class CloseableResource[A](resource: A)(close: A => Unit) {
    def using[B](use: A => B): B = {
      try {
        use(resource)
      }
      finally {
        close(resource)
      }
    }
  }
  
  private implicit def fileWriterCloseable(fw: FileWriter): CloseableResource[FileWriter] = {
    CloseableResource(fw) { _.close() }
  }
  
  private implicit def sourceCloseable(s: Source): CloseableResource[Source] = {
    CloseableResource(s) { _.close() }
  }

  private case class ReqToPath(underlying: Req) {
    def toPath: String = underlying.url.replaceAll("/", "_")
  }
  
  private implicit def Req2ReqToPath(req: Req): ReqToPath = { ReqToPath(req) }
}