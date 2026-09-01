package com.github.nenadjakic.fermentlab.common.dto.culture

import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * Payload for logging a feeding. Recording a feeding also refreshes the culture's feed schedule.
 */
data class CultureFeedingRequest(

    val fedAt: OffsetDateTime? = null,

    @field:PositiveOrZero
    val amount: BigDecimal? = null,

    @field:Size(max = 10)
    val unit: String? = null,

    @field:Size(max = 500)
    val note: String? = null
)
