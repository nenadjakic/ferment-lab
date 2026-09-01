package com.github.nenadjakic.fermentlab.common.dto.batch

import com.github.nenadjakic.fermentlab.data.enum.StagePhase
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

/**
 * Payload for appending a custom stage to a batch timeline.
 */
data class BatchStageAddRequest(

    @field:NotBlank
    @field:Size(max = 150)
    val name: String,

    @field:NotNull
    val phase: StagePhase = StagePhase.CUSTOM,

    @field:Size(max = 2000)
    val instruction: String? = null,

    @field:PositiveOrZero
    val plannedDurationDays: Int = 0
)
