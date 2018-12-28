package threelayer

import cats.Monad
import cats.data.ReaderT
import cats.effect.IO
import threelayer.realworld.Services

case class Application[A](unwrap: ReaderT[IO, Services, A]) {
  def run(services: Services): IO[A] = unwrap.run(services)
}

object Application {

  def apply[A](f: Services => IO[A]): Application[A] = Application(ReaderT[IO, Services, A](f))

  implicit def monadInstance: Monad[Application] = new Monad[Application] {
    override def flatMap[A, B](fa: Application[A])(f: A => Application[B]): Application[B] =
      new Application(fa.unwrap.flatMap { (a: A) =>
        f(a).unwrap
      })
    override def pure[A](x: A): Application[A] = Application(_ => IO.pure(x))
    override def tailRecM[A, B](a: A)(f: A => Application[Either[A, B]]): Application[B] = ???
  }
}


