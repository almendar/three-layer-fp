package threelayer
package domain
import cats.Monad
trait AbExperiments[F[_]] {
  def getVersion(algoArea: String, tag: String): F[Version]
}
object AbExperiments {
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
