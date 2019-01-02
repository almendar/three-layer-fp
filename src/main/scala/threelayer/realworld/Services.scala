package threelayer
package realworld

import java.util.concurrent.ConcurrentHashMap

import cats.Monad
import cats.effect.IO
import threelayer.realworld.http.{MockHttp, MonadHttpClient}
import threelayer.realworld.logging.{JavaLogging, LoggingConfig, MonadLogging}
import threelayer.realworld.metrics.{MockMetrics, MonadMetrics}
import threelayer.realworld.storage.{FileStorage, S3Storage, StorageMonad}

abstract class HttpService[G[_]] {
  type HttpEff[_]
  implicit val a: Monad[HttpEff]
  implicit val MH: MonadHttpClient[HttpEff]
  def runHttp[A]: InterpreterFor[G, HttpEff]
}

abstract class LoggingService[G[_]] {
  type LogEff[_]
  implicit val b: Monad[LogEff]
  implicit val ML: MonadLogging[LogEff]
  def runLogging[A](clazz: Class[_]): InterpreterFor[G, LogEff]
}

abstract class MetricsServic {
  type MetricEff[_]
  implicit val c: Monad[MetricEff]
  implicit val MC: MonadMetrics[MetricEff]

  def runMetrics[A](ma: MetricEff[A]): IO[A]
}

abstract class StorageService {
  type FileEff[_]
  implicit val c: Monad[FileEff]
  implicit val MC: StorageMonad[FileEff]
  def runStorageAction[A](ma: FileEff[A]): IO[A]
}

abstract class Services {
  val s3Client: S3Client
  val http: HttpService[IO]
  val logging: LoggingService[IO]
  val metrics: MetricsServic
  val s3Storage: StorageService
  val fileStorage: StorageService
}

object Services {

  def mockServices(): Services = new Services {

    //Global variables
    // ----------------
    override val s3Client: S3Client = ""
    val metric: MetricsEnv = new ConcurrentHashMap()
    val httpEnv: HttpEnv = new ConcurrentHashMap()
    //----------------

    override val http: HttpService[IO] = new HttpService[IO] {
      override type HttpEff[A] = MockHttp[A]
      override implicit val a: Monad[HttpEff] = MockHttp.monadInstance
      override implicit val MH: MonadHttpClient[HttpEff] = MockHttp.instance

      override def runHttp[A]: InterpreterFor[IO, MockHttp] = new InterpreterFor[IO, MockHttp] {
        override def apply[A](fa: MockHttp[A]): IO[A] = fa.runMockHttp.run(httpEnv)
      }
    }

    override val logging: LoggingService[IO] = new LoggingService[IO] {

      type LogEff[A] = JavaLogging[A]
      implicit val b: Monad[LogEff] = JavaLogging.monadInstance
      implicit val ML: MonadLogging[LogEff] = JavaLogging.instance

      override def runLogging[A](clazz: Class[_]): InterpreterFor[IO, LogEff] = new InterpreterFor[IO, LogEff] {
        override def apply[A](ma: LogEff[A]): IO[A] = ma.run.run(LoggingConfig(clazz))
      }

    }

    override val metrics: MetricsServic = new MetricsServic {

      override type MetricEff[A] = MockMetrics[A]
      override implicit val c: Monad[MetricEff] = MockMetrics.monadInstance
      override implicit val MC: MonadMetrics[MetricEff] = MockMetrics.instance

      override def runMetrics[A](ma: MetricEff[A]): IO[A] = ma.run().run(metric)
    }

    override val s3Storage: StorageService = new StorageService {
      override type FileEff[A] = S3Storage[A]
      override implicit val c: Monad[FileEff] = S3Storage.monadInstance
      override implicit val MC: StorageMonad[FileEff] = S3Storage.instance
      override def runStorageAction[A](ma: FileEff[A]): IO[A] = ma.run.run(s3Client)
    }
    override val fileStorage: StorageService = new StorageService {
      override type FileEff[A] = FileStorage[A]
      override implicit val c: Monad[FileEff] = FileStorage.monadInstance
      override implicit val MC: StorageMonad[FileEff] = FileStorage.instance
      override def runStorageAction[A](ma: FileEff[A]): IO[A] = ma.run.run()
    }
  }
}
