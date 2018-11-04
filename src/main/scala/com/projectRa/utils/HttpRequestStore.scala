package com.projectRa.utils

import dispatch.Req
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
 * Almacenamiento de peticiones http
 * 
 */
trait HttpRequestStore {
  
  /**
   * Obtiene la respuesta ante una petición http
   * 
   * @param req petición http
   * @param ec contexto de ejecución
   * 
   * @return futuro de respuesta si existe
   */
  def getResponse(req: Req)(implicit ec: ExecutionContext): Future[Option[String]]
  
  /**
   * Guarda la respuesta de una petición http
   * 
   * @param req petición http
   * @param bodyResponse respuesta
   * @param ec contexto de ejecución
   * 
   * @return futuro indicando si ya existía la respuesta
   */
  def putResponse(req: Req, bodyResponse: String)(implicit ec: ExecutionContext): Future[Boolean]
}