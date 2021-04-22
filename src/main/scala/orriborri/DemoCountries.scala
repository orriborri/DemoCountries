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
import scala.concurrent.duration._

object DemoCountries extends App {
  var sortK: Option[String] = None
  if (args.length > 0) sortK = Some(args(0))

  val logger = LoggerFactory.getLogger(getClass.getSimpleName)
  implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext = system.executionContext
  val datagetter = new DataGetter()
  val csvWriter = new CsvWriter()
  val flags = new FlaggGetter()

  // needed for the future flatMap/onComplete in the end
  val url = "restcountries.eu/rest/v2/all"
  val countryDataF = datagetter.getCountries(url)
  println("******STATS******")
  countryDataF.onComplete {
    case Success(data) => {
      logger.info("All Json data read and parsed")
      datagetter
        .getStatistic()
        .map({
          case (k, v) =>
            if (k.nonEmpty) println("Region " + k + ":" + v)

        })
      println("******************")
      val sortedData = sortK.getOrElse("") match {
        case "name"       => data.sortBy(_.name)
        case "subregion"  => data.sortBy(_.subregion)
        case "population" => data.sortBy(_.population)
        case _            => data
      }
      flags.getFlaggs(data).onComplete {
        case Success(f)         => println("Done getting flags")
        case Failure(exception) => exception

      }
      val wf = csvWriter.write(
        "countries.csv",
        sortedData
      )

      wf onComplete {
        case Success(x)         => println("Done writing")
        case Failure(exception) => exception
      }

    }
    case Failure(exception) => exception
  }

}

//name, alpha3Code, region, subregion, capital, population
