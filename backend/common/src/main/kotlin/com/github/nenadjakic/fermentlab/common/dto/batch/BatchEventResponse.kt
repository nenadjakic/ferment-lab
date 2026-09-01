package com.github.nenadjakic.fermentlab.common.dto.batch

import com.github.nenadjakic.fermentlab.data.enum.BatchEventType
import java.time.OffsetDateTime
import java.util.UUID

/**
 * One entry of a batch journal.
 */
data class BatchEventResponse(
    val id: UUID,
    val batchId: UUID,
    val batchCode: String,
    val eventType: BatchEventType,
    val occurredAt: OffsetDateTime,
    val title: String,
    val description: String?
)
