package com.projectRa

import com.typesafe.config.ConfigFactory
import dispatch._
import scala.concurrent.ExecutionContext
import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import io.netty.handler.ssl.JdkSslContext
import io.netty.handler.ssl.ClientAuth
import java.security.cert.X509Certificate
import spray.json._
import model.external._
import java.util.Date
import java.text.SimpleDateFormat

class AemetExtractor {
  import AemetExtractor._
  
  lazy val config = ConfigFactory.load
  lazy val baseUrl = config.getString("aemet.url")
  lazy val apiKey = config.getString("aemet.api-key")
  lazy val httpClient = Http.withConfiguration(trustAllCertificates)
  lazy val formatter = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss")
  
  def getStations(implicit ec: ExecutionContext): Future[Seq[Station]] = {
    val urlStations = url(baseUrl) / "valores" / "climatologicos" / "inventarioestaciones" / "todasestaciones"

    for (dataUrl <- httpClient(urlStations.addQueryParameter("api_key", apiKey) OK as.String)
                      .map { _.parseJson.convertTo[ServiceResponse].datos };
         dataResponse <- httpClient(url(dataUrl) OK as.String))
      yield {
        dataResponse.parseJson.convertTo[Seq[model.external.Station]].map { s => 
          Station(s.indicativo, s.nombre, s.provincia, s.indsinop)
        }
      }
  }
  
  def getConventionalObservation(stationId: String)(implicit ec: ExecutionContext): Future[Seq[Observation]] = {
    val conventionalObserv = url(baseUrl) / "observacion" / "convencional" / "datos" / "estacion" / stationId
    for (dataUrl <- httpClient(conventionalObserv.addQueryParameter("api_key", apiKey) OK as.String)
                      .map { _.parseJson.convertTo[ServiceResponse].datos };
         dataResponse <- httpClient(url(dataUrl) OK as.String))
      yield {
        dataResponse.parseJson.convertTo[Seq[model.external.Observation]].map { s => 
          Observation(s.idema, formatter.parse(s.fint), s.inso)
        }
      }  
  }
  
  private def trustAllCertificates(b: Builder): Builder = {
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null,
                    Seq(new X509TrustManager {
                          def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = { }
                          def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = { }
                          def getAcceptedIssuers() = { Array.empty[X509Certificate] }}).toArray,
                    null)
    b.setSslContext(new JdkSslContext(sslContext, true, ClientAuth.NONE))
  }
}

object AemetExtractor {
  case class Station(id: String, name: String, province: String, indsinop: String)
  case class Observation(stationId: String, finishDate: Date, insolationMinutes: Double)
}