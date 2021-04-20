//#full-example
package orriborri

import org.slf4j.LoggerFactory
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

object DemoCountries extends App {
  val logger = LoggerFactory.getLogger(getClass.getSimpleName)
  implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
  implicit val executionContext = system.executionContext

  val datagetter = new DataGetter()
  // needed for the future flatMap/onComplete in the end
  val url = "restcountries.eu/rest/v2/all"
  val dataJson = datagetter.getCountries(url)

}
//name, alpha3Code, region, subregion, capital, population
