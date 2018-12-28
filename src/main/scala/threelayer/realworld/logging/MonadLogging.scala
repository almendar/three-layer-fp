package threelayer
package realworld.logging

abstract class MonadLogging[F[_]] {
  def info(s: String): F[Unit]
  def warning(s: String): F[Unit]
}
