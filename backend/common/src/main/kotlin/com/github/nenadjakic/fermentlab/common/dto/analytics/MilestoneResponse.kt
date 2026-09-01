package com.github.nenadjakic.fermentlab.common.dto.analytics

import java.time.OffsetDateTime
import java.util.UUID

/**
 * An upcoming thing to do: a stage finishing, or a batch reaching its planned end.
 */
data class MilestoneResponse(
    val batchId: UUID,
    val batchCode: String,
    val batchName: String,
    val stageId: UUID?,
    val title: String,
    val dueAt: OffsetDateTime,

    /** Whole days until [dueAt]; negative when the milestone is already overdue. */
    val daysRemaining: Long
)
