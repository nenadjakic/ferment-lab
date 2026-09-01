package com.github.nenadjakic.fermentlab.common.dto.batch

import com.github.nenadjakic.fermentlab.common.dto.reference.FermentTypeSimpleResponse
import com.github.nenadjakic.fermentlab.common.enum.BatchHealth
import com.github.nenadjakic.fermentlab.data.enum.BatchOutcome
import com.github.nenadjakic.fermentlab.data.enum.BatchStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Batch card used on the batch list and on the dashboard.
 *
 * Everything from [dayCount] down is derived on read, so the card always reflects the newest
 * reading without a write having to touch the batch row.
 */
data class BatchResponse(
    val id: UUID,
    val code: String,
    val name: String,
    val fermentType: FermentTypeSimpleResponse,
    val recipeId: UUID?,
    val recipeName: String?,
    val cultureId: UUID?,
    val cultureName: String?,
    val status: BatchStatus,
    val startedOn: LocalDate,
    val expectedEndOn: LocalDate,
    val endedOn: LocalDate?,
    val container: String?,
    val batchSize: BigDecimal?,
    val batchSizeUnit: String?,
    val location: String?,
    val rating: Int?,
    val outcome: BatchOutcome?,

    /** Days elapsed since the batch started. */
    val dayCount: Long,

    /** Days between start and planned finish. */
    val plannedDurationDays: Long,

    /** Elapsed share of the plan as a percentage; exceeds 100 for an overdue batch. */
    val progressPercent: Int,

    /** `true` when an open batch has passed its planned finish. */
    val overdue: Boolean,

    /** Derived state combining the schedule and the newest readings. */
    val health: BatchHealth,

    /** Short explanation of why [health] is what it is, shown as a tooltip. */
    val healthReason: String?,

    /** Newest reading of every metric logged on this batch. */
    val latestMeasurements: List<MeasurementResponse>,

    /** Number of unacknowledged alerts attached to this batch. */
    val openAlertCount: Int
)
