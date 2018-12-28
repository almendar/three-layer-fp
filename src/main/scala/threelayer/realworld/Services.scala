package threelayer
package realworld

import java.util.concurrent.ConcurrentHashMap

import cats.Monad
import cats.effect.IO
import cats.effect.concurrent.Ref
import threelayer.realworld.metrics.MockMetrics
import threelayer.realworld.http.{MockHttp, MonadHttpClient}
import threelayer.realworld.logging.{JavaLogging, LoggingConfig, MonadLogging}
import threelayer.realworld.metrics.MonadMetrics
import threelayer.realworld.storage.{FileStorage, S3Storage, StorageMonad}
import threelayer.{HttpEnv, HttpState, MetricsState, S3Client}

trait InterpreterFor[F[_], Eff[_], A] {
  def apply(fa: Eff[A]): F[A]
}

abstract class HttpService {
  type HttpEff[_]
  implicit val a: Monad[HttpEff]
  implicit val MH: MonadHttpClient[HttpEff]

  def runHttp[A](ma: HttpEff[A]): IO[A]
}

abstract class LoggingService {
  type LogEff[_]
  implicit val b: Monad[LogEff]
  implicit val ML: MonadLogging[LogEff]

  def runLogging[A](clazz: Class[_])(ma: LogEff[A]): IO[A]
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
  val http: HttpService
  val logging: LoggingService
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

    override val http: HttpService = new HttpService {
      override type HttpEff[A] = MockHttp[A]
      override implicit val a: Monad[HttpEff] = MockHttp.monadInstance
      override implicit val MH: MonadHttpClient[HttpEff] = MockHttp.instance

      override def runHttp[A](ma: HttpEff[A]): IO[A] = ma.runMockHttp.run(httpEnv)
    }

    override val logging: LoggingService = new LoggingService {

      type LogEff[A] = JavaLogging[A]
      implicit val b: Monad[LogEff] = JavaLogging.monadInstance
      implicit val ML: MonadLogging[LogEff] = JavaLogging.instance

      override def runLogging[A](clazz: Class[_])(ma: LogEff[A]): IO[A] = ma.run.run(LoggingConfig(clazz))

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
