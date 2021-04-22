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
import akka.stream.OverflowStrategy.fail
import akka.stream.OverflowStrategy
import scala.collection.mutable.Map
import orriborri.Model.Country

class DataGetter()(
    implicit val system: ActorSystem[Any],
    implicit val ec: ExecutionContextExecutor
) {
  var statistic: Map[String, Int] = Map()
  def getStatistic(): Map[String, Int] = statistic
  def getCountries(
      url: String
  ): Future[Seq[Country]] = {

    implicit val profileFormat = jsonFormat7(Country)
    statistic = Map()
    val logger = LoggerFactory.getLogger(getClass.getSimpleName)
    val httpClient = Http().outgoingConnection("restcountries.eu")

    val response = Source
      .single(HttpRequest(uri = Uri("/rest/v2/all")))

    val jsonFraming: Flow[ByteString, ByteString, NotUsed] =
      JsonFraming.objectScanner(Int.MaxValue)

    val stringToContry: Flow[ByteString, Country, NotUsed] = {
      Flow[ByteString]
        .map(s => {
          s.utf8String.parseJson.convertTo[Country]
        })
    }

    val resToString: Flow[HttpResponse, ByteString, NotUsed] = {
      Flow[HttpResponse].flatMapConcat { r =>
        r.entity.dataBytes
      }
    }
    val stats = Flow[Country].map(c => {
      statistic(c.region) = statistic.getOrElse(c.capital, 0) + c.population
      c
    })

    response
      .via(httpClient)
      .via(resToString)
      .via(jsonFraming)
      .via(stringToContry)
      .via(stats)
      .runWith(Sink.seq[Country])
  }

}
