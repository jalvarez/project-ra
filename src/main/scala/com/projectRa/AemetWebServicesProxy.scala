package com.projectRa

import dispatch.Req
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait AemetWebServicesProxy {
  def getResponseUsingApiKey(req: Req)(implicit ec: ExecutionContext): Future[String]
}