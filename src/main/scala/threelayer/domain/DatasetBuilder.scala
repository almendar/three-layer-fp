package threelayer
package domain
import java.time.LocalDateTime

trait Dataset

abstract class DatasetBuilder[F[_]] {
  def readDataset(from: LocalDateTime, to: LocalDateTime, algoArea: String, tag: String): F[Dataset]
}
