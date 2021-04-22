package orriborri

import akka.stream.scaladsl.Flow
import akka.stream.alpakka.csv.scaladsl.CsvFormatting
import akka.util.ByteString
import scala.collection.immutable
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.FileIO
import akka.NotUsed
import java.nio.file.Paths
import akka.stream.scaladsl.FileIO
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContextExecutor
import akka.actor.typed.ActorSystem
import orriborri.Model._

class CsvWriter()(
    implicit val system: ActorSystem[Any],
    implicit val ec: ExecutionContextExecutor
) {

  val csvFormat: Flow[immutable.Seq[String], ByteString, NotUsed] =
    CsvFormatting.format()
  val countryToSeq: Flow[Country, immutable.Seq[String], NotUsed] =
    Flow[Country].map(c =>
      Seq(
        c.name,
        c.alpha3Code,
        c.region,
        c.subregion,
        c.capital,
        c.population.toString()
      )
    )

  def write(fileName: String, cS: Seq[Country]) = {
    val file = Paths.get("countries.csv")
    Source(cS)
      .via(countryToSeq)
      .via(csvFormat)
      .runWith(FileIO.toPath(file))
  }
}
