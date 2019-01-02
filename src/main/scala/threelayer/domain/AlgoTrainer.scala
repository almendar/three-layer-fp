package threelayer
package domain

trait AlgoTrainer[F[_]] {
  def train(version: Version): F[Unit]
}
object AlgoTrainer {

  implicit def algoTrainApp: AlgoTrainer[Application] =
    (version: Version) =>
      Application { services =>
        import services.logging._
        runLogging(AlgoTrainer.getClass)(ML.info(s"Training $version"))
    }
}
