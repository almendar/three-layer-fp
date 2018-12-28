package threelayer
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import threelayer.realworld.Services
import threelayer.realworld.metrics.{IncCounter, MetricOp, ReadValue}
import threelayer.realworld.storage.StorageMonad
import threelayer.realworld.storage.StorageMonad.{DataHolder, Location}

import scala.util.Random

object Main extends IOApp {

  def logWarning(msg: String, ctx: Class[_] = this.getClass): Application[Unit] = Application[Unit] {
    services: Services =>
      import services.logging._
      runLogging[Unit](ctx)(
        for {
          _ <- ML.warning(msg)
        } yield ()
      )
  }

  def bumpCounterMetrics(op: MetricOp) = Application[MetricValue] { service: Services =>
    import service.metrics._
    runMetrics[MetricValue] {
      for {
        i <- MC.counter(op)

      } yield i
    }
  }

  def readCounter(rv: ReadValue) = Application[MetricValue] { service: Services =>
    import service.metrics._
    runMetrics[MetricValue] {
      for {
        i <- MC.counter(rv)
      } yield i
    }
  }

  def downloadS3KeyFromWebService: Application[Data] = Application { services: Services =>
    import services.http._
    runHttp {
      for {
        _ <- MH.post("http://xyz.com", Random.alphanumeric.take(Random.nextInt(15)).mkString)
        i <- MH.get("http://xyz.com")
      } yield i
    }
  }

  def storeOnLocalDisk(location: Location, dh: DataHolder): Application[Long] = Application { services: Services =>
    import services.fileStorage._
    runStorageAction {
      MC.save(location, dh)
    }
  }

  def readFromS3(location: StorageMonad.Location): Application[DataHolder] = Application { services: Services =>
    import services.s3Storage._
    runStorageAction { MC.read(location) }
  }

  val program: Application[ExitCode] = for {
    key <- downloadS3KeyFromWebService
    _ <- logWarning(s"Key downloaded: ${key}")
    _ <- bumpCounterMetrics(IncCounter("Http", key.length))
    m1 <- readCounter(ReadValue("Http"))
    _ <- logWarning(s"Number of bytes downloaded $m1")
    dh <- readFromS3(Location(s"s3://rpp/test/bucket/$key"))
    bytesSaved <- storeOnLocalDisk(Location("output.txt"), dh)
    _ <- logWarning(s"Number of bytes saved $bytesSaved")
  } yield ExitCode.Success

  override def run(args: List[String]): IO[ExitCode] = {
    program.run(Services.mockServices())
  }
}
