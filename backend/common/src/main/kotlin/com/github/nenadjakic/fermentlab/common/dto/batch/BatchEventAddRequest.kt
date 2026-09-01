package com.github.nenadjakic.fermentlab.common.dto.batch

import com.github.nenadjakic.fermentlab.data.enum.BatchEventType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

/**
 * Payload for adding a journal entry to a batch.
 */
data class BatchEventAddRequest(

    @field:NotNull
    val eventType: BatchEventType,

    @field:NotBlank
    @field:Size(max = 150)
    val title: String,

    @field:Size(max = 2000)
    val description: String? = null,

    val occurredAt: OffsetDateTime? = null
)
