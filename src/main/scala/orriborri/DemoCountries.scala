//#full-example
package orriborri

import org.slf4j.LoggerFactory
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import scala.util.Success
import scala.util.Failure
import akka.stream.ActorMaterializer
import scala.collection.mutable.Buffer
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
object DemoCountries extends App {
  var sortK: Option[String] = None
  if (args.length > 0) sortK = Some(args(0))

  val logger = LoggerFactory.getLogger(getClass.getSimpleName)
  implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext = system.executionContext
  var fl = Buffer[Future[Any]]()
  val datagetter = new DataGetter()
  val csvWriter = new CsvWriter()

  // needed for the future flatMap/onComplete in the end
  val url = "restcountries.eu/rest/v2/all"
  val countryDataF = datagetter.getCountries(url)
  fl.append(countryDataF)
  countryDataF.onComplete {
    case Success(data) => {
      logger.info("All Json data read and parsed")
      val sortedData = sortK.getOrElse("") match {
        case "name"       => data.sortBy(_.name)
        case "subregion"  => data.sortBy(_.subregion)
        case "population" => data.sortBy(_.population)
        case _            => data
      }
      val wf = csvWriter.write(
        "countries.csv",
        sortedData
      )
      wf onComplete {
        case Success(_)         => logger.info("Done writing csv")
        case Failure(exception) => exception
      }
      fl.append(wf)
    }
    case Failure(exception) => exception
  }

  val f2 = Future(fl.foreach(f => Await.result(f, Duration.Inf)))
  f2 onComplete {
    case Success(_)         => logger.info("All done!")
    case Failure(exception) => exception
  }
  System.exit(0)
}

//name, alpha3Code, region, subregion, capital, population
