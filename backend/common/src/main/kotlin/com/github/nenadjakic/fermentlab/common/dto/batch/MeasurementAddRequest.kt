package com.github.nenadjakic.fermentlab.common.dto.batch

import com.github.nenadjakic.fermentlab.data.enum.MetricType
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * Payload for logging a reading.
 *
 * When [unit] is omitted the metric's default unit is used, and when [recordedAt] is omitted the
 * reading is timestamped now.
 */
data class MeasurementAddRequest(

    @field:NotNull
    val metric: MetricType,

    @field:NotNull
    val value: BigDecimal,

    @field:Size(max = 15)
    val unit: String? = null,

    val recordedAt: OffsetDateTime? = null,

    @field:Size(max = 500)
    val note: String? = null
)
