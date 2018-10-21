package com.projectRa.model

import spray.json.DefaultJsonProtocol

package object external extends DefaultJsonProtocol {
  case class ServiceResponse(datos: String)
  implicit val serviceResponseFormat = jsonFormat1(ServiceResponse)
  
  case class Station (latitud: String,
                      provincia: String,
                      altitud: String,
                      indicativo: String,
                      nombre: String,
                      indsinop: String,
                      longitud: String)
  implicit val stationFormat = jsonFormat7(Station)
  
  case class Observation (idema: String,
                          fint: String,
                          inso: Double)
  implicit val observationFormat = jsonFormat3(Observation)
}