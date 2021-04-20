package orriborri

import scala.concurrent.ExecutionContextExecutor
import akka.actor.typed.ActorSystem
import org.slf4j.LoggerFactory
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.Http
import scala.concurrent.Future
import akka.http.scaladsl.model.HttpResponse
import scala.util.Success
import scala.util.Failure
import akka.http.scaladsl.unmarshalling.Unmarshal
import spray.json._
import akka.stream.scaladsl.Flow
import akka.NotUsed
import play.api.libs.json.Json
import spray.json._
import spray.json._
import DefaultJsonProtocol._
import com.fasterxml.jackson.annotation.JsonValue
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.http.scaladsl.model.Uri
import akka.util.ByteString
import akka.stream.scaladsl.JsonFraming

case class Country(
    name: String,
    alpha3Code: String,
    subregion: String,
    capital: String,
    population: Int
)

class DataGetter()(
    implicit val system: ActorSystem[Any],
    implicit val ec: ExecutionContextExecutor
) {

  def getCountries(
      url: String
  ) {
    implicit val profileFormat = jsonFormat5(Country)

    val logger = LoggerFactory.getLogger(getClass.getSimpleName)
    //https://github.com/spray/spray-json
    val parallelism = 10
    val httpClient = Http().outgoingConnection("restcountries.eu")

    val response = Source
      .single(HttpRequest(uri = Uri("/rest/v2/all")))

    val jsonFraming: Flow[ByteString, ByteString, NotUsed] =
      JsonFraming.objectScanner(Int.MaxValue)

    val resToString: Flow[HttpResponse, ByteString, NotUsed] = {
      Flow[HttpResponse].flatMapConcat { r =>
        r.entity.dataBytes
      }
    }

    val stringToContry: Flow[ByteString, Country, NotUsed] = {
      Flow[ByteString].map(s => {
        s.utf8String.parseJson.convertTo[Country]
      })
    }

    val countryToSeq: Flow[Country, String, NotUsed] =
      Flow[Country].map(_.toString())

    response
      .via(httpClient)
      .via(resToString)
      .via(jsonFraming)
      .via(stringToContry)
      .runWith(Sink.foreach(println(_)))
  }

}
