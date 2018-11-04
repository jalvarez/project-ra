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
import scala.util.Try

trait AemetHttpExtractor extends AemetExtractor with DateFormatters { this: AemetWebServicesProxy =>
  import AemetHttpExtractor._
  import AemetExtractor._
  
  lazy val baseUrl = config.getString("aemet.url")
  lazy val apiKey = config.getString("aemet.api-key")
  
  override def getStations(implicit ec: ExecutionContext): Future[Seq[Station]] = {
    val urlStations = url(baseUrl) / "valores" / "climatologicos" / "inventarioestaciones" / "todasestaciones"

    getResponseUsingApiKey(urlStations).map { response => 
      response.parseJson.convertTo[Seq[model.external.Station]].map { s => 
        Station(s.indicativo, s.nombre, s.provincia, s.indsinop)
      }
    }
  }
  
  override def getConventionalObservation(stationId: String)
                                         (implicit ec: ExecutionContext): Future[Seq[Observation]] = {
    val conventionalObserv = url(baseUrl) / "observacion" / "convencional" / "datos" / "estacion" / stationId
    getResponseUsingApiKey(conventionalObserv).map { response =>
      response.parseJson.convertTo[Seq[model.external.Observation]].map { s => 
        Observation(s.idema, formatterTimestamp.parse(s.fint), Some(s.inso))
      }
    }  
  }
  
  override def getDiaryClimateValues(stationId: String, from: Date, to: Date)
                                    (implicit ec: ExecutionContext): Future[Seq[DiaryClimateValue]] = {

    implicit def date2String(date: Date): String = formatterTimestampUTC.format(date)
    def string2Double(str: String): Option[Double] = Try(str.replaceAll(",", ".").toDouble).toOption
    
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
  
  override def getDiaryPrediction(provinceCode: Int, day: Date)
                                 (implicit ec: ExecutionContext): Future[DiaryPrediction] = {
    implicit def date2String(date: Date): String = formatterDay.format(date)
    
    val diaryPredictions = url(baseUrl) / "prediccion" / "provincia" / "hoy" / provinceCode / "elaboracion" / day
    getResponseUsingApiKey(diaryPredictions).map { response =>
      DiaryPrediction(provinceCode, day, response)
    }
  }  
}

object AemetHttpExtractor {
  lazy val logger = LoggerFactory.getLogger("com.projectRa.AemetHttpExtractor")
  lazy val config = ConfigFactory.load
}