package com.projectRa

import com.typesafe.config.ConfigFactory
import dispatch._
import scala.concurrent.ExecutionContext

import spray.json._
import model.external._
import java.util.Date
import utils.DateFormatters
import org.slf4j.LoggerFactory
import utils.HttpConfigTrustAllCertificates

class AemetExtractor extends DateFormatters with HttpConfigTrustAllCertificates {
  import AemetExtractor._
  
  lazy val config = ConfigFactory.load
  lazy val baseUrl = config.getString("aemet.url")
  lazy val apiKey = config.getString("aemet.api-key")
  lazy val httpClient = Http.withConfiguration(trustAllCertificates)
  
  /**
   * Obtiene las estaciones
   * 
   * @return secuencia de estaciones
   */
  def getStations(implicit ec: ExecutionContext): Future[Seq[Station]] = {
    val urlStations = url(baseUrl) / "valores" / "climatologicos" / "inventarioestaciones" / "todasestaciones"

    getResponseUsingApiKey(urlStations).map { response => 
      response.parseJson.convertTo[Seq[model.external.Station]].map { s => 
        Station(s.indicativo, s.nombre, s.provincia, s.indsinop)
      }
    }
  }
  
  /**
   * Obtiene los valores observados de una estación en el momento actual
   * 
   * @param stationId id. del la estación
   * @return secuencia de observaciones registradas
   */
  def getConventionalObservation(stationId: String)(implicit ec: ExecutionContext): Future[Seq[Observation]] = {
    val conventionalObserv = url(baseUrl) / "observacion" / "convencional" / "datos" / "estacion" / stationId
    getResponseUsingApiKey(conventionalObserv).map { response =>
      response.parseJson.convertTo[Seq[model.external.Observation]].map { s => 
        Observation(s.idema, formatterTimestamp.parse(s.fint), s.inso)
      }
    }  
  }
  
  /**
   * Obtiene los valores climatológicos de una estación en un intervalo de tiempo
   * 
   * @param stationId id. de la estación
   * @param from desde
   * @param to hasta
   * 
   * @return una secuencia de valores climatológicos
   */
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
  
  /**
   * Obtiene la predicción en lenguaje natural de una provincia para un día
   * 
   * @param provinceCode código de provincia
   * @param day día de la predicción
   * 
   * @return predicción diaria
   */
  def getDiaryPrediction(provinceCode: Int, day: Date)
                        (implicit ec: ExecutionContext): Future[DiaryPrediction] = {
    implicit def date2String(date: Date): String = formatterDay.format(date)
    
    val diaryPredictions = url(baseUrl) / "prediccion" / "provincia" / "hoy" / provinceCode / "elaboracion" / day
    getResponseUsingApiKey(diaryPredictions).map { response =>
      DiaryPrediction(provinceCode, day, response)
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
}

object AemetExtractor {
  lazy val logger = LoggerFactory.getLogger("com.projectRa.AemetExtractor")
  
  /**
   * Datos de la estación metereológica
   * 
   * @param id identificador
   * @param name nombre
   * @param provincia código de la provincia
   * @param indsinop indicador sinóptico
   */
  case class Station(id: String, name: String, province: String, indsinop: String)
  
  /**
   * Valores observados en una estación en un momento dado
   * 
   * @param stationId id. del la estación
   * @param finishDate fecha final de la observación
   * @param insolationMinutes minutos de insolación de la última hora
   */
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
                               
  /**
   * Predicción diaria por provincia en formato texto
   * 
   * @param provinceCode código de provincia
   * @param day 
   * @param predicción                            
   */
  case class DiaryPrediction(provinceCode: Int, day: Date, prediction: String)
}