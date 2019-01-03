package threelayer.realworld.http

import cats.{Applicative, Monad}
import cats.data.ReaderT
import cats.effect.{IO, LiftIO}
import cats.mtl.ApplicativeAsk
import threelayer.{Data, HttpEnv, Url}

abstract class MockHttp[A] {
  self =>
  def runMockHttp: ReaderT[IO, HttpEnv, A]

  def map[B](f: A => B): MockHttp[B] = new MockHttp[B] {
    override def runMockHttp: ReaderT[IO, HttpEnv, B] = self.runMockHttp.map(f)
  }

  def flatMap[B](f: A => MockHttp[B]): MockHttp[B] = new MockHttp[B] {
    override def runMockHttp: ReaderT[IO, HttpEnv, B] = self.runMockHttp.flatMap(a => f(a).runMockHttp)
  }
}

object MockHttp {

  implicit val appAsk: ApplicativeAsk[MockHttp, HttpEnv] = new ApplicativeAsk[MockHttp, HttpEnv] {
    override val applicative: Applicative[MockHttp] = monadInstance

    override def ask: MockHttp[HttpEnv] = new MockHttp[HttpEnv] {
      override def runMockHttp: ReaderT[IO, HttpEnv, HttpEnv] = ReaderT[IO, HttpEnv, HttpEnv](x => IO.pure(x))
    }
    override def reader[A](f: HttpEnv => A): MockHttp[A] = new MockHttp[A] {
      override def runMockHttp: ReaderT[IO, HttpEnv, A] = ReaderT[IO, HttpEnv, A](x => IO.delay(f(x)))
    }
  }

  implicit val liftIO: LiftIO[MockHttp] = new LiftIO[MockHttp] {
    override def liftIO[A](ioa: IO[A]): MockHttp[A] = new MockHttp[A] {
      override def runMockHttp: ReaderT[IO, HttpEnv, A] = ReaderT[IO, HttpEnv, A](env => ioa)
    }
  }

  implicit def monadInstance: Monad[MockHttp] = new Monad[MockHttp] {
    override def flatMap[A, B](fa: MockHttp[A])(f: A => MockHttp[B]): MockHttp[B] = fa.flatMap(f)

    override def pure[A](x: A): MockHttp[A] = new MockHttp[A] {
      override def runMockHttp: ReaderT[IO, HttpEnv, A] = ReaderT[IO, HttpEnv, A](_ => IO.pure(x))
    }

    override def tailRecM[A, B](a: A)(f: A => MockHttp[Either[A, B]]): MockHttp[B] = ???
  }

  implicit def instance: MonadHttpClient[MockHttp] = new MonadHttpClient[MockHttp] {
    override def get(url: Url): MockHttp[Data] = new MockHttp[Data] {
      override def runMockHttp: ReaderT[IO, HttpEnv, Data] = ReaderT { env: HttpEnv =>
        IO(env.getOrDefault(url, "404 NotFound"))
      }
    }

    override def post[A: ToJson](url: Url, a: A): MockHttp[Data] = new MockHttp[Data] {
      override def runMockHttp: ReaderT[IO, HttpEnv, Data] = ReaderT { env: HttpEnv =>
        IO(env.put(url, implicitly[ToJson[A]].encode(a)))
      }
    }
  }
}
