//#full-example
package orriborri

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.client.RequestBuilding.Get

import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.http.scaladsl.unmarshalling.Unmarshal
import play.api.libs.json._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.Http
import org.slf4j.LoggerFactory

object Main extends App {
  def main(): Unit = {
    val logger = LoggerFactory.getLogger(getClass.getSimpleName)
    implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    val url = "https://restcountries.eu/rest/v2/all"
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = url
    )

    val result = Http()
      .singleRequest(request)
      .transformWith {
        case Success(res) =>
          val data = Unmarshal(res).to[String]

          data.map { d =>
            val usefulInfo = Json.parse(d)

            println(usefulInfo)

            usefulInfo
          }
        case Failure(e) =>
          println(e)

          Future.failed(e)
      }
    result.onComplete {
      case Success(data)      => print(data)
      case Failure(exception) =>
    }
  }
}
