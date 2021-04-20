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


trait MyJsonProtocol extends DefaultJsonProtocol {
  implicit val countyFormat = jsonFormat5(Country)
}
// object MyJsonProtocol extends DefaultJsonProtocol {
//   implicit object counryJsonFormat extends RootJsonFormat[Country] {

//     def read(value: JsValue) = {
//       value.asJsObject.getFields(
//         "name",
//         "alpha3Code",
//         "subregion",
//         "capital",
//         "population"
//       ) match {
//         case Seq(
//             JsString(name),
//             JsString(alpha3Code),
//             JsString(subregion),
//             JsString(capital),
//             JsNumber(population)
//             ) =>
//           new Country(name, alpha3Code, subregion, capital, population.toInt)
//         case _ => throw new DeserializationException("")
//       }
//     }
//   }
// }
