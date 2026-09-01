package com.github.nenadjakic.fermentlab.common.dto.analytics

import com.github.nenadjakic.fermentlab.data.enum.BatchOutcome
import com.github.nenadjakic.fermentlab.data.enum.MetricType
import java.time.LocalDate
import java.util.UUID

/**
 * Side by side comparison of several batches.
 *
 * Every series is indexed by day offset rather than by absolute date, so batches started weeks apart
 * overlay cleanly on the same chart.
 */
data class BatchComparisonResponse(
    val metrics: List<MetricType>,
    val batches: List<ComparedBatchResponse>
) {

    /**
     * One batch in the comparison, with its own line per requested metric.
     */
    data class ComparedBatchResponse(
        val batchId: UUID,
        val code: String,
        val name: String,
        val fermentTypeName: String,
        val accentColor: String?,
        val startedOn: LocalDate,
        val dayCount: Long,
        val rating: Int?,
        val outcome: BatchOutcome?,
        val series: List<MetricSeriesResponse>
    )
}
