package com.projectRa

import utils.HttpRequestCache
import dispatch.Req
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait AemetWebServicesProxyWithCache extends AemetWebServicesProxy {
  val underlying: AemetWebServicesProxy 
  val cache: HttpRequestCache
  
  override def getResponseUsingApiKey(req: Req)(implicit ec: ExecutionContext): Future[String] = {
    for (cacheResponse <- cache.getResponse(req);
         response <- cacheResponse match {
                                     case Some(r) => Future.successful(r)
                                     case None => underlying.getResponseUsingApiKey(req)
                                   })
      yield cacheResponse.getOrElse(response)
  }
}

object AemetWebServicesProxyWithCache {
  
}