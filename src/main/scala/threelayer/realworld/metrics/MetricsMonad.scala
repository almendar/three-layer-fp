package threelayer
package realworld.metrics

sealed trait MetricOp

case class IncCounter(name: MetricName, of: MetricValue) extends MetricOp

case class DecCounter(name: MetricName, of: MetricValue) extends MetricOp

case class ReadValue(name: MetricName) extends MetricOp

abstract class MonadMetrics[F[_]] {
  def counter(op: MetricOp): F[MetricValue]
}
