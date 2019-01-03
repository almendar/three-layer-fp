package threelayer
package domain
import cats.Monad
import cats.effect.IO
import threelayer.realworld.http.MonadHttpClient
trait AbExperiments[F[_]] {
  def getVersion(algoArea: String, tag: String): F[Version]
}
object AbExperiments {

  def example[F[_]]: Tranformations[MonadHttpClient, AbExperiments] =
    new Tranformations[MonadHttpClient, AbExperiments] {
      override type Target = F
      override def apply[A](eff1: MonadHttpClient[F]): AbExperiments[F] = new AbExperiments[F] {
        override def getVersion(algoArea: String, tag: String): F[Version] = eff1.get(s"http://$algoArea/$tag")
      }
    }

  implicit def abExperiments: AbExperiments[Application] =
    (algoArea: String, tag: String) =>
      Application { services =>
        import services.http._
        import services.logging._
        for {
          _ <- runLogging(AbExperiments.getClass)(ML.info("Getting data for training"))
          v <- runHttp(MH.get(s"$algoArea/$tag"))
        } yield v
    }
}
