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
import DefaultJsonProtocol._
import com.fasterxml.jackson.annotation.JsonValue
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.http.scaladsl.model.Uri
import akka.util.ByteString
import akka.stream.scaladsl.JsonFraming
import scala.collection.immutable
import java.nio.file.Paths
import akka.stream.scaladsl.FileIO

case class Country(
    name: String,
    alpha3Code: String,
    subregion: String,
    capital: String,
    population: Int,
    flag: String
)

class DataGetter()(
    implicit val system: ActorSystem[Any],
    implicit val ec: ExecutionContextExecutor
) {

  def getCountries(
      url: String
  ): Future[Seq[Country]] = {
    implicit val profileFormat = jsonFormat6(Country)

    val logger = LoggerFactory.getLogger(getClass.getSimpleName)
    //https://github.com/spray/spray-json
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
    // val downloadFlags: Flow[Country, (Country, ByteString), NotUsed] = {
    //   Flow[Country].map(c => {
    //     (c, Http().ClientLayer.Get(c.flag))
    //   })
    // }

    response
      .via(httpClient)
      .via(resToString)
      .via(jsonFraming)
      .via(stringToContry)
      .runWith(Sink.seq[Country])

  }

  // }.via(countryToSeq)
  //   .via(csvFormat)
  //   .runWith(FileIO.toPath(file))

}
