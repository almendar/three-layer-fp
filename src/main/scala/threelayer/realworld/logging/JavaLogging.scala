package threelayer
package realworld.logging

import java.util.logging.Logger

import cats.Monad
import cats.data.ReaderT
import cats.effect.IO

case class LoggingConfig(context: Class[_])

abstract class JavaLogging[A] {
  self =>
  def run: ReaderT[IO, LoggingConfig, A]

  def map[B](f: A => B): JavaLogging[B] = new JavaLogging[B] {
    override def run: ReaderT[IO, LoggingConfig, B] = self.run.map(f)
  }

  def flatMap[B](f: A => JavaLogging[B]): JavaLogging[B] = new JavaLogging[B] {
    override def run: ReaderT[IO, LoggingConfig, B] = self.run.flatMap(a => f(a).run)
  }
}

object JavaLogging {

  implicit val monadInstance = new Monad[JavaLogging] {
    override def pure[A](x: A): JavaLogging[A] = new JavaLogging[A] {
      override def run: ReaderT[IO, LoggingConfig, A] = ReaderT[IO, LoggingConfig, A](_ => IO.pure(x))
    }

    override def flatMap[A, B](fa: JavaLogging[A])(f: A => JavaLogging[B]): JavaLogging[B] = fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => JavaLogging[Either[A, B]]): JavaLogging[B] = ???
  }

  implicit def instance: MonadLogging[JavaLogging] = new MonadLogging[JavaLogging] {

    override def info(s: String): JavaLogging[Unit] = new JavaLogging[Unit] {
      override def run: ReaderT[IO, LoggingConfig, Unit] =
        ReaderT(x => IO(Logger.getLogger(x.context.getName.toString).info(s)))
    }

    override def warning(s: String): JavaLogging[Unit] = new JavaLogging[Unit] {
      override def run: ReaderT[IO, LoggingConfig, Unit] =
        ReaderT(x => IO(Logger.getLogger(x.context.getName.toString).warning(s)))
    }
  }
}
