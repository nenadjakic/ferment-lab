package com.github.nenadjakic.fermentlab.common.dto.batch

import com.github.nenadjakic.fermentlab.data.enum.MetricType
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

/**
 * A single reading taken from a batch.
 *
 * @property dayOffset days between the batch start and this reading, which is what the comparison
 * view plots on its x axis so batches started weeks apart line up.
 */
data class MeasurementResponse(
    val id: UUID,
    val batchId: UUID,
    val metric: MetricType,
    val value: BigDecimal,
    val unit: String,
    val recordedAt: OffsetDateTime,
    val dayOffset: Long,
    val note: String?
)
