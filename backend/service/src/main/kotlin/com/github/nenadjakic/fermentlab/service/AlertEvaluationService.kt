package com.github.nenadjakic.fermentlab.service

import com.github.nenadjakic.fermentlab.common.extension.forDisplay
import com.github.nenadjakic.fermentlab.data.enum.AlertSeverity
import com.github.nenadjakic.fermentlab.data.enum.AlertType
import com.github.nenadjakic.fermentlab.data.enum.BatchStatus
import com.github.nenadjakic.fermentlab.data.enum.MetricType
import com.github.nenadjakic.fermentlab.data.repository.BatchRepository
import com.github.nenadjakic.fermentlab.data.repository.BatchStageRepository
import com.github.nenadjakic.fermentlab.data.repository.CultureRepository
import com.github.nenadjakic.fermentlab.data.repository.MeasurementRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/**
 * Turns the state of the data into alerts.
 *
 * This is the engine behind the smart notifications and the over-fermentation warnings. It is
 * deliberately side-effect-light: every alert is written through a deterministic dedupe key, so the
 * job can run as often as it likes without spamming the notification centre.
 */
@Service
class AlertEvaluationService(
    private val batchRepository: BatchRepository,
    private val batchStageRepository: BatchStageRepository,
    private val measurementRepository: MeasurementRepository,
    private val cultureRepository: CultureRepository,
    private val alertService: AlertService
) {

    private val logger = LoggerFactory.getLogger(AlertEvaluationService::class.java)

    /**
     * Runs every rule and returns how many alerts were written or refreshed.
     */
    @Transactional
    fun evaluate(): Int {
        val raised = evaluateBatches() + evaluateCultures()
        logger.info("Alert evaluation finished, {} alert(s) raised or refreshed.", raised)
        return raised
    }

    private fun evaluateBatches(): Int {
        val batches = batchRepository.findAllByStatusInOrderByStartedOnDesc(
            listOf(BatchStatus.ACTIVE, BatchStatus.PAUSED)
        )
        if (batches.isEmpty()) {
            return 0
        }

        val ids = batches.mapNotNull { it.id }
        val latestByBatch = measurementRepository.findLatestPerMetric(ids)
            .groupBy { requireNotNull(it.batch.id) }
            .mapValues { (_, readings) -> readings.associateBy { it.metric } }
        val stagesByBatch = batchStageRepository.findAllByBatchIdInOrderByPositionAsc(ids)
            .groupBy { requireNotNull(it.batch.id) }

        var raised = 0
        val today = LocalDate.now()
        val now = OffsetDateTime.now()

        batches.forEach { batch ->
            val id = requireNotNull(batch.id)
            val latest = latestByBatch[id].orEmpty()

            // 1. Over-fermentation: the newest pH has crossed the sour limit for the style.
            val ph = latest[MetricType.PH]?.value
            val sourLimit = batch.fermentType.overFermentationPh
            if (ph != null && sourLimit != null && ph < sourLimit) {
                alertService.raiseForBatch(
                    batch = batch,
                    alertType = AlertType.OVER_FERMENTATION_RISK,
                    severity = AlertSeverity.CRITICAL,
                    message = "${batch.name} is at pH ${ph.forDisplay()}, past the " +
                        "${sourLimit.forDisplay()} sour limit for ${batch.fermentType.name}. " +
                        "Refrigerate or harvest it now.",
                    dueAt = now,
                    dedupeKey = "$id:OVER_FERMENTATION"
                )
                raised++
            }

            // 2. Harvest window: the batch has reached or passed its planned finish.
            val daysOverdue = ChronoUnit.DAYS.between(batch.expectedEndOn, today)
            when {
                daysOverdue > OVERDUE_CRITICAL_DAYS -> {
                    alertService.raiseForBatch(
                        batch = batch,
                        alertType = AlertType.STAGE_OVERDUE,
                        severity = AlertSeverity.CRITICAL,
                        message = "${batch.name} is $daysOverdue days past its planned finish.",
                        dueAt = batch.expectedEndOn.atTime(12, 0).atOffset(now.offset),
                        dedupeKey = "$id:OVERDUE"
                    )
                    raised++
                }

                daysOverdue >= 0 -> {
                    alertService.raiseForBatch(
                        batch = batch,
                        alertType = AlertType.HARVEST_WINDOW,
                        severity = AlertSeverity.WARNING,
                        message = "${batch.name} has reached its planned finish. Taste it and decide.",
                        dueAt = batch.expectedEndOn.atTime(12, 0).atOffset(now.offset),
                        dedupeKey = "$id:HARVEST_WINDOW"
                    )
                    raised++
                }
            }

            // 3. Temperature excursion on the newest reading.
            val temperature = latest[MetricType.TEMPERATURE]?.value
            val tempMin = batch.fermentType.targetTempMinC
            val tempMax = batch.fermentType.targetTempMaxC
            if (temperature != null && ((tempMin != null && temperature < tempMin) || (tempMax != null && temperature > tempMax))) {
                alertService.raiseForBatch(
                    batch = batch,
                    alertType = AlertType.TEMPERATURE_OUT_OF_RANGE,
                    severity = AlertSeverity.WARNING,
                    message = "${batch.name} was last logged at ${temperature.forDisplay()} °C, outside the " +
                        "${tempMin?.forDisplay() ?: "-"}-${tempMax?.forDisplay() ?: "-"} °C window " +
                        "for ${batch.fermentType.name}.",
                    dueAt = now,
                    dedupeKey = "$id:TEMPERATURE"
                )
                raised++
            }

            // 4. The log has gone quiet.
            val lastReading = latest.values.maxOfOrNull { it.recordedAt }
            val quietDays = lastReading
                ?.let { ChronoUnit.DAYS.between(it.toLocalDate(), today) }
                ?: ChronoUnit.DAYS.between(batch.startedOn, today)
            if (quietDays >= STALE_MEASUREMENT_DAYS) {
                alertService.raiseForBatch(
                    batch = batch,
                    alertType = AlertType.MEASUREMENT_STALE,
                    severity = AlertSeverity.INFO,
                    message = "No readings logged on ${batch.name} for $quietDays days.",
                    dueAt = now,
                    dedupeKey = "$id:STALE"
                )
                raised++
            }

            // 5. A running stage has passed its planned end.
            stagesByBatch[id]
                .orEmpty()
                .filter { it.completedAt == null && it.startedAt != null && it.plannedDurationDays > 0 }
                .forEach { stage ->
                    val dueAt = requireNotNull(stage.startedAt).plusDays(stage.plannedDurationDays.toLong())
                    if (dueAt.isBefore(now)) {
                        alertService.raiseForBatch(
                            batch = batch,
                            alertType = AlertType.MILESTONE_DUE,
                            severity = AlertSeverity.WARNING,
                            message = "Stage '${stage.name}' on ${batch.name} is due.",
                            dueAt = dueAt,
                            dedupeKey = "${stage.id}:STAGE_DUE"
                        )
                        raised++
                    }
                }
        }

        return raised
    }

    private fun evaluateCultures(): Int {
        val today = LocalDate.now()
        val now = OffsetDateTime.now()
        var raised = 0

        cultureRepository.findAllByActiveTrueAndFeedIntervalDaysNotNull().forEach { culture ->
            val dueOn = culture.nextFeedDueOn ?: return@forEach
            if (dueOn.isAfter(today)) {
                return@forEach
            }

            val daysLate = ChronoUnit.DAYS.between(dueOn, today)
            alertService.raiseForCulture(
                culture = culture,
                alertType = AlertType.CULTURE_FEED_DUE,
                severity = if (daysLate > CULTURE_FEED_CRITICAL_DAYS) AlertSeverity.CRITICAL else AlertSeverity.INFO,
                message = if (daysLate == 0L) {
                    "${culture.name} is due a feed today."
                } else {
                    "${culture.name} is $daysLate day(s) overdue a feed."
                },
                dueAt = dueOn.atTime(9, 0).atOffset(now.offset),
                dedupeKey = "${culture.id}:FEED:$dueOn"
            )
            raised++
        }

        return raised
    }

    private companion object {

        /** Days past the planned finish before an overdue batch escalates to critical. */
        const val OVERDUE_CRITICAL_DAYS = 2L

        /** Days without a reading before the log counts as stale. */
        const val STALE_MEASUREMENT_DAYS = 5L

        /** Days a feed can slip before it escalates to critical. */
        const val CULTURE_FEED_CRITICAL_DAYS = 3L
    }
}
