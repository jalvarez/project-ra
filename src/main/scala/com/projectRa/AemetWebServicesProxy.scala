package com.projectRa

import dispatch.Req
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
 * Proxy para acceder a los servicios web de la Aemet
 */
trait AemetWebServicesProxy {
  
  /**
   * Respuesta usando api-key
   * 
   * @param req petición http
   * @param ec contexto de ejecución
   * 
   * @return respuesta de la petición asíncrona
   */
  def getResponseUsingApiKey(req: Req)(implicit ec: ExecutionContext): Future[String]
}