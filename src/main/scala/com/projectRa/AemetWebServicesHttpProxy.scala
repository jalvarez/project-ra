package com.projectRa

import dispatch._
import scala.concurrent.ExecutionContext
import spray.json._
import org.slf4j.LoggerFactory
import utils.HttpConfigTrustAllCertificates

trait AemetWebServicesHttpProxy extends AemetWebServicesProxy with HttpConfigTrustAllCertificates {
  import AemetWebServicesHttpProxy._
  import model.external._

  val apiKey: String
  
  lazy val httpClient = Http.withConfiguration(trustAllCertificates)
  
  def getResponseUsingApiKey(req: Req)(implicit ec: ExecutionContext): Future[String] = {
    for (dataUrl <- httpClient(req.addQueryParameter("api_key", apiKey) OK as.String)
                      .map { _.parseJson.convertTo[ServiceResponse].datos };
         dataResponse <- httpClient(url(dataUrl) OK as.String))
      yield {
        if (logger.isDebugEnabled) logger.debug(dataResponse)
        dataResponse
      }
  }
}

object AemetWebServicesHttpProxy extends DefaultJsonProtocol {
  lazy val logger = LoggerFactory.getLogger("com.projectRa.AemetWebServicesProxy")
}