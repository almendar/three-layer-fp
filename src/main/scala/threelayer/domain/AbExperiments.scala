package threelayer
package domain

trait AbExperiments[F[_]] {
  def getVersion(algoArea: String, tag: String): F[Version]
}
