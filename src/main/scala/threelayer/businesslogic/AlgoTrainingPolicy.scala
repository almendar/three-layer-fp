package threelayer
package businesslogic

trait AlgoTrainingPolicy {

  def shouldTrain(algoArea: String, lastTrained: DateTime, now: DateTime): Boolean = algoArea match {
    case "1" => now.h - lastTrained.h > 6
    case "2" => now.h - lastTrained.h > 1
    case _   => true
  }
}

object AlgoTrainingPolicy extends AlgoTrainingPolicy
