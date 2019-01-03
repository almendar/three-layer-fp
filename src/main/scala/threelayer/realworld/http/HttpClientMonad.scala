package threelayer
package realworld.http
import cats.Monad
import cats.effect.IO

trait ToJson[A] {
  def encode(a: A): Json
}

object ToJson {
  implicit val stringInstance: ToJson[String] = s => s
}

abstract class MonadHttpClient[F[_]] {
  def get(url: Url): F[Data]
  def post[A: ToJson](url: Url, a: A): F[Data]
}