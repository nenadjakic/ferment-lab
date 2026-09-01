package com.github.nenadjakic.fermentlab.common.dto.analytics

import com.github.nenadjakic.fermentlab.data.enum.MetricType
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * One chart line: every reading of a single metric for a single batch, in time order.
 */
data class MetricSeriesResponse(
    val metric: MetricType,
    val unit: String,

    /** Lower bound of the healthy window for the batch's style, when one is defined. */
    val targetMin: BigDecimal?,

    /** Upper bound of the healthy window for the batch's style, when one is defined. */
    val targetMax: BigDecimal?,

    /** Threshold past which the batch counts as over-fermented, when the metric has one. */
    val dangerThreshold: BigDecimal?,

    val points: List<MetricPointResponse>
) {

    /**
     * A single point on the line.
     *
     * @property dayOffset days since the batch started, used as the x axis in the comparison view.
     */
    data class MetricPointResponse(
        val recordedAt: OffsetDateTime,
        val dayOffset: Long,
        val value: BigDecimal
    )
}
