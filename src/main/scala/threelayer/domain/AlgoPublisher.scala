package threelayer
package domain
import threelayer.businesslogic.TrainConfiguration

trait AlgoPublisher[F[_]] {
  def isPublished(params: TrainConfiguration): F[Option[String]]
  def publish(onlineJar: Location, params: TrainConfiguration): F[String]

}
