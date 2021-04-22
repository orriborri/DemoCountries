package orriborri

import akka.actor.typed.ActorSystem
import scala.concurrent.ExecutionContextExecutor
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow
import orriborri.Model.Country
import akka.util.ByteString
import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Path
import akka.actor.typed.delivery.internal.ProducerControllerImpl.Request
import java.nio.file.Paths
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Sink
import akka.stream.IOResult
import akka.compat.Future
import scala.concurrent.Future

class FlaggGetter()(
    implicit val system: ActorSystem[Any],
    implicit val ec: ExecutionContextExecutor
) {

  val baseUrl = "https://restcountries.eu"
  def getFlaggs(countriesS: Seq[Country]) = {
    println("flaggs download started")
    val source = Source(countriesS)
    val connectionFlow: Flow[HttpRequest, HttpResponse, _] =
      Http().outgoingConnection(baseUrl.replace("https://", ""))

    def pathToRequest(c: Country) = {
      HttpRequest(uri = Uri.Empty.withPath(Path(c.flag.replace(baseUrl, ""))))
    }
    val countryToReq: Flow[Country, HttpRequest, NotUsed] = {
      Flow[Country] map pathToRequest
    }

    val reqToByte: Flow[HttpResponse, ByteString, NotUsed] = {
      Flow[HttpResponse].flatMapConcat { r =>
        r.entity.dataBytes
      }

    }

    val file = Paths.get("countries.csv")
    val writeToFile: Flow[(ByteString, Country), Future[IOResult], NotUsed] = {
      Flow[(ByteString, Country)].mapAsync(parallelism = 8) {
        case (b, c) => {
          val file = Paths.get("Flags/" + c.name + ".svg")
          Future(Source.single(b).runWith(FileIO.toPath(file)))

        }
      }
    }

    source
      .via(countryToReq)
      .via(connectionFlow)
      .via(reqToByte)
      .zip(Source(countriesS))
      .via(writeToFile)
      .runWith(Sink.seq[Future[IOResult]])
  }
}
