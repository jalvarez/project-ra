package com.projectRa

import utils.HttpRequestStore
import dispatch.Req
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
 * Proxy para acceder a los servicios web de la Aemet con cache
 */
trait AemetWebServicesProxyWithCache extends AemetWebServicesProxy {
  val underlying: AemetWebServicesProxy 
  val requestStore: HttpRequestStore
  
  override def getResponseUsingApiKey(req: Req)(implicit ec: ExecutionContext): Future[String] = {
    for (cacheResponse <- requestStore.getResponse(req);
         response <- cacheResponse match {
                                     case Some(r) => Future.successful(r)
                                     case None => 
                                       for (r <- underlying.getResponseUsingApiKey(req);
                                            _ <- requestStore.putResponse(req, r))
                                         yield r
                                   })
      yield cacheResponse.getOrElse(response)
  }
}