package com.projectRa

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class AemetExtractorSpec extends FlatSpec with Matchers with ScalaFutures {
  implicit val defaultPatience = PatienceConfig(10 seconds)
      
  val extractor = new AemetExtractor()
  
  /*
  it should "return data of stations" in {
    extractor.getStations.futureValue should not be empty  
  }
  * 
  */
  
  it should "return last conventional observations of a station" in {
    val stationId = "C430E"
    
    extractor.getConventionalObservation(stationId).futureValue should not be empty
  }
}