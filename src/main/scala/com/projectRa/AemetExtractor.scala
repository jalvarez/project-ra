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
import utils.DateFormatters
import org.slf4j.LoggerFactory

class AemetExtractor extends DateFormatters {
  import AemetExtractor._
  
  lazy val config = ConfigFactory.load
  lazy val baseUrl = config.getString("aemet.url")
  lazy val apiKey = config.getString("aemet.api-key")
  lazy val httpClient = Http.withConfiguration(trustAllCertificates)
  
  def getStations(implicit ec: ExecutionContext): Future[Seq[Station]] = {
    val urlStations = url(baseUrl) / "valores" / "climatologicos" / "inventarioestaciones" / "todasestaciones"

    getResponseUsingApiKey(urlStations).map { response => 
      response.parseJson.convertTo[Seq[model.external.Station]].map { s => 
        Station(s.indicativo, s.nombre, s.provincia, s.indsinop)
      }
    }
  }
  
  def getConventionalObservation(stationId: String)(implicit ec: ExecutionContext): Future[Seq[Observation]] = {
    val conventionalObserv = url(baseUrl) / "observacion" / "convencional" / "datos" / "estacion" / stationId
    getResponseUsingApiKey(conventionalObserv).map { response =>
      response.parseJson.convertTo[Seq[model.external.Observation]].map { s => 
        Observation(s.idema, formatterTimestamp.parse(s.fint), s.inso)
      }
    }  
  }
  
  def getDiaryClimateValues(stationId: String, from: Date, to: Date)
                           (implicit ec: ExecutionContext): Future[Seq[DiaryClimateValue]] = {

    implicit def date2String(date: Date): String = formatterTimestampUTC.format(date)
    def string2Double(str: String): Double = str.replaceAll(",", ".").toDouble
    
    val diaryClimateValues = url(baseUrl) / "valores" / "climatologicos" / "diarios" / "datos" / 
                                          "fechaini" / from / "fechafin"/ to / "estacion" / stationId
                                          
    getResponseUsingApiKey(diaryClimateValues).map { response =>
      response.parseJson.convertTo[Seq[model.external.DiaryClimateValue]].map { s =>
        DiaryClimateValue(s.indicativo,
                          formatterDay.parse(s.fecha),
                          string2Double(s.sol), 
                          string2Double(s.tmed),
                          string2Double(s.prec))
      }
    }
  }
  
  private def getResponseUsingApiKey(req: Req)(implicit ec: ExecutionContext): Future[String] = {
    for (dataUrl <- httpClient(req.addQueryParameter("api_key", apiKey) OK as.String)
                      .map { _.parseJson.convertTo[ServiceResponse].datos };
         dataResponse <- httpClient(url(dataUrl) OK as.String))
      yield {
        if (logger.isDebugEnabled) logger.debug(dataResponse)
        dataResponse
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
  lazy val logger = LoggerFactory.getLogger("com.projectRa.AemetExtractor")
  
  case class Station(id: String, name: String, province: String, indsinop: String)
  
  case class Observation(stationId: String, finishDate: Date, insolationMinutes: Double)
 
  /**
   * Climatologías diarias
   * 
   * @param stationId
   * @param day
   * @param incolationHours
   * @param averagetTemperature
   * @param precipitationMm
   */
  case class DiaryClimateValue(stationId: String,
                               day: Date,
                               insolationHours: Double,
                               averageTemperature: Double,
                               precipitationMm: Double)
                               
}