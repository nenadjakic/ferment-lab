package com.github.nenadjakic.fermentlab.common.dto.analytics

import com.github.nenadjakic.fermentlab.common.dto.NameValueResponse
import com.github.nenadjakic.fermentlab.common.dto.alert.AlertResponse
import com.github.nenadjakic.fermentlab.common.dto.batch.BatchEventResponse
import com.github.nenadjakic.fermentlab.common.dto.batch.BatchResponse

/**
 * Everything the dashboard renders, assembled in one request.
 */
data class DashboardSummaryResponse(
    val activeBatchCount: Long,
    val plannedBatchCount: Long,
    val completedBatchCount: Long,
    val activeCultureCount: Long,
    val openAlertCount: Long,

    /** Mean rating across every rated batch, `null` until something has been rated. */
    val averageRating: Double?,

    /** How many batches exist per ferment style. */
    val batchesPerFermentType: List<NameValueResponse>,

    /** Every open batch, newest first. */
    val activeBatches: List<BatchResponse>,

    /** Open batches that are over-fermented, drifting or overdue. */
    val attentionBatches: List<BatchResponse>,

    /** The next things to do, soonest first. */
    val upcomingMilestones: List<MilestoneResponse>,

    /** Latest journal entries across all batches. */
    val recentEvents: List<BatchEventResponse>,

    /** Unacknowledged alerts, most urgent first. */
    val openAlerts: List<AlertResponse>
)
