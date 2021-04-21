package orriborri.countriesDataType

import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

//name, alpha3Code, region, subregion, capital, population
case class Country(
    name: String,
    alpha3Code: String,
    subregion: String,
    capital: String,
    population: Int
)

//http://www.danieluzunu.com/2015/10/04/reflecting-the-values-of-class-members-in-scala/
//This is need in order to sort the csv by
