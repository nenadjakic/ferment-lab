package com.github.nenadjakic.fermentlab.common.extension

import com.github.nenadjakic.fermentlab.common.dto.batch.BatchEventResponse
import com.github.nenadjakic.fermentlab.common.dto.batch.BatchResponse
import com.github.nenadjakic.fermentlab.common.dto.batch.BatchStageResponse
import com.github.nenadjakic.fermentlab.common.dto.batch.MeasurementResponse
import com.github.nenadjakic.fermentlab.common.enum.BatchHealth
import com.github.nenadjakic.fermentlab.common.enum.StageStatus
import com.github.nenadjakic.fermentlab.data.entity.batch.Batch
import com.github.nenadjakic.fermentlab.data.entity.batch.BatchEvent
import com.github.nenadjakic.fermentlab.data.entity.batch.BatchStage
import com.github.nenadjakic.fermentlab.data.entity.batch.Measurement
import com.github.nenadjakic.fermentlab.data.enum.BatchStatus
import com.github.nenadjakic.fermentlab.data.enum.MetricType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/**
 * Days a batch may run past its planned finish before it counts as over-fermented.
 */
private const val OVERDUE_TOLERANCE_DAYS = 2L

/**
 * Days without a new reading before an open batch is flagged for attention.
 */
private const val STALE_MEASUREMENT_DAYS = 5L

/**
 * Renders a reading for humans.
 *
 * Values come back from the database at the column's scale, so a pH of 3.02 arrives as `3.0200`.
 * Trailing zeros are dropped, and [java.math.BigDecimal.toPlainString] keeps whole numbers out of
 * scientific notation.
 */
fun BigDecimal.forDisplay(): String = stripTrailingZeros().toPlainString()

fun Measurement.toResponse(): MeasurementResponse =
    MeasurementResponse(
        id = requireNotNull(id),
        batchId = requireNotNull(batch.id),
        metric = metric,
        value = value,
        unit = unit,
        recordedAt = recordedAt,
        dayOffset = ChronoUnit.DAYS.between(batch.startedOn, recordedAt.toLocalDate()).coerceAtLeast(0),
        note = note
    )

fun BatchEvent.toResponse(): BatchEventResponse =
    BatchEventResponse(
        id = requireNotNull(id),
        batchId = requireNotNull(batch.id),
        batchCode = batch.code,
        eventType = eventType,
        occurredAt = occurredAt,
        title = title,
        description = description
    )

fun BatchStage.toResponse(): BatchStageResponse {
    val dueAt = startedAt?.plusDays(plannedDurationDays.toLong())
    val status = when {
        completedAt != null -> StageStatus.COMPLETED
        startedAt == null -> StageStatus.PENDING
        dueAt != null && dueAt.isBefore(OffsetDateTime.now()) -> StageStatus.OVERDUE
        else -> StageStatus.ACTIVE
    }

    return BatchStageResponse(
        id = requireNotNull(id),
        position = position,
        name = name,
        phase = phase,
        instruction = instruction,
        plannedDurationDays = plannedDurationDays,
        startedAt = startedAt,
        completedAt = completedAt,
        note = note,
        status = status,
        dueAt = dueAt,
        daysRemaining = dueAt?.let { ChronoUnit.DAYS.between(OffsetDateTime.now(), it) }
    )
}

/**
 * Builds the batch card.
 *
 * @param latestMeasurements newest reading per metric for this batch, as returned by
 * [com.github.nenadjakic.fermentlab.data.repository.MeasurementRepository.findLatestPerMetric].
 * @param openAlertCount number of unacknowledged alerts attached to the batch.
 */
fun Batch.toResponse(
    latestMeasurements: List<Measurement> = emptyList(),
    openAlertCount: Int = 0
): BatchResponse {
    val (health, reason) = deriveHealth(latestMeasurements)

    return BatchResponse(
        id = requireNotNull(id),
        code = code,
        name = name,
        fermentType = fermentType.toSimpleResponse(),
        recipeId = recipe?.id,
        recipeName = recipe?.name,
        cultureId = culture?.id,
        cultureName = culture?.name,
        status = status,
        startedOn = startedOn,
        expectedEndOn = expectedEndOn,
        endedOn = endedOn,
        container = container,
        batchSize = batchSize,
        batchSizeUnit = batchSizeUnit,
        location = location,
        rating = rating,
        outcome = outcome,
        dayCount = dayCount,
        plannedDurationDays = plannedDurationDays,
        progressPercent = progressPercent,
        overdue = status.isOpen && status != BatchStatus.PLANNED && LocalDate.now().isAfter(expectedEndOn),
        health = health,
        healthReason = reason,
        latestMeasurements = latestMeasurements.map { it.toResponse() },
        openAlertCount = openAlertCount
    )
}

/**
 * Over-fermentation detection.
 *
 * The checks run in order of severity: a reading past the style's sour limit beats being overdue,
 * which beats a temperature excursion, which beats a stale log. A batch that is inside its schedule
 * and inside every window is on track.
 */
private fun Batch.deriveHealth(latestMeasurements: List<Measurement>): Pair<BatchHealth, String?> {
    if (status == BatchStatus.PLANNED) {
        return BatchHealth.PLANNED to "Starts on $startedOn."
    }
    if (!status.isOpen) {
        return BatchHealth.CLOSED to null
    }

    val latestByMetric = latestMeasurements.associateBy { it.metric }

    val ph = latestByMetric[MetricType.PH]?.value
    val sourLimit = fermentType.overFermentationPh
    if (ph != null && sourLimit != null && ph < sourLimit) {
        return BatchHealth.OVER_FERMENTED to
            "pH ${ph.forDisplay()} is below the ${sourLimit.forDisplay()} sour limit for ${fermentType.name}."
    }

    val daysOverdue = ChronoUnit.DAYS.between(expectedEndOn, LocalDate.now())
    if (daysOverdue > OVERDUE_TOLERANCE_DAYS) {
        return BatchHealth.OVER_FERMENTED to "$daysOverdue days past the planned finish."
    }

    val temperature = latestByMetric[MetricType.TEMPERATURE]?.value
    val tempMin = fermentType.targetTempMinC
    val tempMax = fermentType.targetTempMaxC
    if (temperature != null && tempMin != null && temperature < tempMin) {
        return BatchHealth.ATTENTION to
            "Last temperature ${temperature.forDisplay()} °C is below the ${tempMin.forDisplay()} °C target."
    }
    if (temperature != null && tempMax != null && temperature > tempMax) {
        return BatchHealth.ATTENTION to
            "Last temperature ${temperature.forDisplay()} °C is above the ${tempMax.forDisplay()} °C target."
    }

    val lastReading = latestMeasurements.maxOfOrNull { it.recordedAt }
    if (lastReading == null) {
        return BatchHealth.ATTENTION to "No readings logged yet."
    }
    val daysSinceReading = ChronoUnit.DAYS.between(lastReading.toLocalDate(), LocalDate.now())
    if (daysSinceReading >= STALE_MEASUREMENT_DAYS) {
        return BatchHealth.ATTENTION to "No readings for $daysSinceReading days."
    }

    if (daysOverdue >= 0 || dayCount >= plannedDurationDays) {
        return BatchHealth.READY to "At or past the planned finish - taste it and decide."
    }

    return BatchHealth.ON_TRACK to null
}
