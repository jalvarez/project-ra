package com.projectRa.utils

import dispatch.Req
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait HttpRequestCache {
  def getResponse(req: Req)(implicit ec: ExecutionContext): Future[Option[String]]
}