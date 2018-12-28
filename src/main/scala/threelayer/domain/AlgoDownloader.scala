package threelayer.domain
import threelayer.Version

trait AlgoDownloader[F[_]] {
  def downloadAlgo(algoArea: String, tag: String): F[Version]
}
