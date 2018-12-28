package threelayer
package realworld.storage
import threelayer.Data
import cats.Monad

abstract class StorageMonad[F[_]: Monad] {
  def save(path: Location, dh: DataHolder): F[Long]
  def read(path: Location): F[DataHolder]
}
