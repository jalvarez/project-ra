package com.projectRa

import scala.concurrent.ExecutionContext
import java.util.Date
import scala.concurrent.Future

trait AemetExtractor {
  import AemetExtractor._
  
  /**
   * Obtiene las estaciones
   * 
   * @return secuencia de estaciones
   */  
  def getStations(implicit ec: ExecutionContext): Future[Seq[Station]]
  
  /**
   * Obtiene los valores observados de una estación en el momento actual
   * 
   * @param stationId id. del la estación
   * @return secuencia de observaciones registradas
   */
  def getConventionalObservation(stationId: String)(implicit ec: ExecutionContext): Future[Seq[Observation]]
  
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
                           (implicit ec: ExecutionContext): Future[Seq[DiaryClimateValue]]
  
  /**
   * Obtiene la predicción en lenguaje natural de una provincia para un día
   * 
   * @param provinceCode código de provincia
   * @param day día de la predicción
   * 
   * @return predicción diaria
   */
  def getDiaryPrediction(provinceCode: Int, day: Date)
                        (implicit ec: ExecutionContext): Future[DiaryPrediction]  
}
  
object AemetExtractor {
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
  case class Observation(stationId: String,
                         finishDate: Date,
                         insolationMinutes: Option[Double])
 
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
                               insolationHours: Option[Double],
                               averageTemperature: Option[Double],
                               precipitationMm: Option[Double])
                               
  /**
   * Predicción diaria por provincia en formato texto
   * 
   * @param provinceCode código de provincia
   * @param day 
   * @param predicción                            
   */
  case class DiaryPrediction(provinceCode: Int, day: Date, prediction: String)  
}