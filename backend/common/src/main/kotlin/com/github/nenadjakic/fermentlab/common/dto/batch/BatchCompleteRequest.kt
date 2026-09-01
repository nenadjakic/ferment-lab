package com.github.nenadjakic.fermentlab.common.dto.batch

import com.github.nenadjakic.fermentlab.data.enum.BatchOutcome
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

/**
 * Payload for closing a batch. Completing a batch also closes any stage still running.
 */
data class BatchCompleteRequest(

    @field:NotNull
    val outcome: BatchOutcome,

    @field:Min(1)
    @field:Max(5)
    val rating: Int? = null,

    val endedOn: LocalDate? = null,

    @field:Size(max = 2000)
    val notes: String? = null
)
