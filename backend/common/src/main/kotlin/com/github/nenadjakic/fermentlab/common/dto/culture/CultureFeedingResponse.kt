package com.github.nenadjakic.fermentlab.common.dto.culture

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

/**
 * One entry of a culture's care log.
 */
data class CultureFeedingResponse(
    val id: UUID,
    val cultureId: UUID,
    val fedAt: OffsetDateTime,
    val amount: BigDecimal?,
    val unit: String?,
    val note: String?
)
