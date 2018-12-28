package threelayer
package realworld.metrics

import cats.Monad
import cats.data.ReaderT
import cats.effect.IO

abstract class MockMetrics[A] {
  self =>
  def run(): ReaderT[IO, MetricsEnv, A]

  def map[B](f: A => B): MockMetrics[B] = new MockMetrics[B] {
    override def run: ReaderT[IO, MetricsEnv, B] = self.run.map(f)
  }

  def flatMap[B](f: A => MockMetrics[B]): MockMetrics[B] = new MockMetrics[B] {
    override def run: ReaderT[IO, MetricsEnv, B] = self.run.flatMap(a => f(a).run)
  }
}

object MockMetrics {

  implicit def monadInstance: Monad[MockMetrics] = new Monad[MockMetrics] {
    override def pure[A](x: A): MockMetrics[A] = { () =>
      ReaderT[IO, MetricsEnv, A](_ => IO.pure(x))
    }

    override def flatMap[A, B](fa: MockMetrics[A])(f: A => MockMetrics[B]): MockMetrics[B] = fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => MockMetrics[Either[A, B]]): MockMetrics[B] = ???
  }

  implicit def instance: MonadMetrics[MockMetrics] =
    (op: MetricOp) =>
      () =>
        ReaderT { ms: MetricsEnv =>
          op match {
            case IncCounter(name, value) => IO(ms.compute(name, (t: String, u: MetricValue) => u + value))
            case DecCounter(name, value) => IO(ms.compute(name, (t: String, u: MetricValue) => u - value))
            case ReadValue(name)         => IO(ms.get(name))
          }
    }

}
