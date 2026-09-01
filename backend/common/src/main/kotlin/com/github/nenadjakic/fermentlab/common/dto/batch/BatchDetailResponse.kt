package com.github.nenadjakic.fermentlab.common.dto.batch

import com.github.nenadjakic.fermentlab.common.dto.alert.AlertResponse
import com.github.nenadjakic.fermentlab.common.dto.analytics.MetricSeriesResponse

/**
 * Everything the batch detail page needs in a single round trip: the card, the timeline, the
 * journal, the chart series and the open alerts.
 */
data class BatchDetailResponse(
    val batch: BatchResponse,
    val notes: String?,
    val stages: List<BatchStageResponse>,
    val events: List<BatchEventResponse>,
    val series: List<MetricSeriesResponse>,
    val alerts: List<AlertResponse>,
    val measurementCount: Long
)
