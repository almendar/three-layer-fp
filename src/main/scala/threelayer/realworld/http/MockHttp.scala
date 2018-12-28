package threelayer.realworld.http

import cats.Monad
import cats.data.ReaderT
import cats.effect.IO
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