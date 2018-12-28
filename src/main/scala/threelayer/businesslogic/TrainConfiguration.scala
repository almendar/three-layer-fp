package threelayer
package businesslogic
import java.time.{LocalDateTime, Duration}

case class TrainConfiguration(tag: String,
                              runId: String,
                              algoArea: String,
                              trainUntilTime: LocalDateTime,
                              trainingDataSpan: Duration)
