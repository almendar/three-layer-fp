import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import cats.Id
import java.util.{Map => JMap}

import cats.data.ReaderT
import cats.effect.IO

package object threelayer {

  type Url         = String
  type Json        = String
  type Data        = String
  type MetricName  = String
  type MetricValue = Int
  type S3Secret    = String
  type S3Access    = String
  type S3Server    = String
  //This should be the REAL s3 client from AWS package
  type S3Client     = String
  type HttpEnv      = Id[HttpState]
  type HttpState    = ConcurrentHashMap[Url, Data]
  type MetricsEnv   = Id[MetricsState]
  type MetricsState = ConcurrentHashMap[MetricName, MetricValue]

  type ExecutionEnv[Env, R] = ReaderT[IO, Env, R]
  type HttpExecEnv[R]       = ReaderT[IO, HttpEnv, R]
  type S3ExecEnv[R]         = ExecutionEnv[S3Client, R]
  type FileExecEnv[R]       = ExecutionEnv[Unit, R]
  implicit def fromFuncToReader[Env, R](f: Env => IO[R]): ReaderT[IO, Env, R] = ReaderT[IO, Env, R](f)

  object ExecutionEnv {
    def apply[Env, R](f: Env => IO[R]): ExecutionEnv[Env, R] = ReaderT[IO, Env, R](f)
  }
}
