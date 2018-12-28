import java.io.{FileInputStream, InputStream}
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

import cats.Id
import cats.data.ReaderT
import cats.effect.IO
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream

package object threelayer extends Time with DataAccess {

  type Url = String
  type Json = String
  type Data = String
  type MetricName = String
  type MetricValue = Int
  type S3Secret = String
  type S3Access = String
  type S3Server = String
  //This should be the REAL s3 client from AWS package
  type S3Client = String
  type HttpEnv = Id[HttpState]
  type HttpState = ConcurrentHashMap[Url, Data]
  type MetricsEnv = Id[MetricsState]
  type MetricsState = ConcurrentHashMap[MetricName, MetricValue]

  type ExecutionEnv[Env, R] = ReaderT[IO, Env, R]
  type HttpExecEnv[R] = ReaderT[IO, HttpEnv, R]
  type S3ExecEnv[R] = ExecutionEnv[S3Client, R]
  type FileExecEnv[R] = ExecutionEnv[Unit, R]
  implicit def fromFuncToReader[Env, R](f: Env => IO[R]): ReaderT[IO, Env, R] = ReaderT[IO, Env, R](f)

  object ExecutionEnv {
    def apply[Env, R](f: Env => IO[R]): ExecutionEnv[Env, R] = ReaderT[IO, Env, R](f)
  }

  //Business logic
  type Version = String
  type Location = String

}

trait DataAccess {
  abstract class DataHolder extends AutoCloseable {
    type Holder
    protected val h: Holder
    def stream: InputStream
    override def close(): Unit = stream.close()
  }

  object DataHolder {
    def apply(bytes: Array[Byte]): DataHolder = new DataHolder {
      override type Holder = Array[Byte]
      override protected val h: Array[Byte] = bytes
      override def stream: InputStream = new ByteInputStream(h, h.length)
    }

    def apply(path: Path): DataHolder = new DataHolder {
      override type Holder = java.io.File
      override protected val h = path.toFile
      override def stream: InputStream = new FileInputStream(h)
    }

    def apply(s: String): DataHolder = new DataHolder {
      override type Holder = String
      override protected val h: String = s
      override def stream: InputStream = new ByteInputStream(h.getBytes, h.getBytes.length)
    }
  }
}

trait Time {
  final case class Date(y: Int, m: Int, d: Int)
  final case class DateTime(d: Date, h: Int, m: Int, s: Int)
  final case class Duration(from: Int, to: Int, of: String = "hours")

  def today(): IO[Date] = IO {
    val ld = LocalDateTime.now()
    Date(ld.getYear, ld.getMonthValue, ld.getDayOfMonth)
  }

  def now(): IO[DateTime] = IO {
    val ld = LocalDateTime.now()
    DateTime(Date(ld.getYear, ld.getMonthValue, ld.getDayOfMonth), ld.getHour, ld.getMinute, ld.getSecond)
  }
}
