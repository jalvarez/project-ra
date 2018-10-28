package com.projectRa

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.Date
import java.util.Calendar

class AemetHttpExtractorSpec extends FlatSpec with Matchers with ScalaFutures {
  implicit val defaultPatience = PatienceConfig(10 seconds)
      
  val extractor = new AemetHttpExtractor with AemetWebServicesHttpProxy
  
  it should "return data of stations" in {
    extractor.getStations.futureValue should not be empty  
  }
  
  it should "return last conventional observations of a station" in {
    val stationId = "C430E" // IZAÑA
    
    extractor.getConventionalObservation(stationId).futureValue should not be empty
  }
  
  it should "return diary climatological values of a station between two days" in new WithDateOperations {
    val stationId = "C449C" // STA.CRUZ DE TENERIFE
    val today = new Date()
    val fourDaysAgo = today - 4
    val fiveDaysAgo = today - 5
    
    extractor.getDiaryClimateValues(stationId, fiveDaysAgo, fourDaysAgo).futureValue should not be empty
  }
  
  it should "return diary predictions of a province" in new WithDateOperations {
    val provinceCode = 38 // STA.CRUZ DE TENERIFE
    val yesterday = new Date() - 1
    
    extractor.getDiaryPrediction(provinceCode, yesterday).futureValue.prediction should not be empty
  }
  
  trait WithDateOperations {
    case class DateWithArithmeticOperations(original: Date) {
      val calendar = Calendar.getInstance
      
      def + (days: Int): Date = {
        calendar.setTime(original)
        calendar.add(Calendar.DATE, days)
        calendar.getTime
      }
      
      def - (days: Int): Date = this.+(-days)
    }
    
    implicit def date2dateWitAdd(d: Date): DateWithArithmeticOperations = DateWithArithmeticOperations(d)
  }
}