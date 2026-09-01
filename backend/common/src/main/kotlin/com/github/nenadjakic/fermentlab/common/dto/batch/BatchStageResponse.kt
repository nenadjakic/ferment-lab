package com.github.nenadjakic.fermentlab.common.dto.batch

import com.github.nenadjakic.fermentlab.common.enum.StageStatus
import com.github.nenadjakic.fermentlab.data.enum.StagePhase
import java.time.OffsetDateTime
import java.util.UUID

/**
 * One phase of a batch timeline, with the countdown the frontend renders as a progress ring.
 */
data class BatchStageResponse(
    val id: UUID,
    val position: Int,
    val name: String,
    val phase: StagePhase,
    val instruction: String?,
    val plannedDurationDays: Int,
    val startedAt: OffsetDateTime?,
    val completedAt: OffsetDateTime?,
    val note: String?,
    val status: StageStatus,

    /** Moment the stage is planned to finish, `null` while it has not started. */
    val dueAt: OffsetDateTime?,

    /** Whole days left until [dueAt]; negative once the stage is overdue. */
    val daysRemaining: Long?
)
