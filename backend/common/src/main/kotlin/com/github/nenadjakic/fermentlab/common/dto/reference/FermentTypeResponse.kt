package com.github.nenadjakic.fermentlab.common.dto.reference

import com.github.nenadjakic.fermentlab.data.enum.FermentCategory
import java.math.BigDecimal
import java.util.UUID

/**
 * Full reference record for a style of ferment, including the target windows the analytics use.
 */
data class FermentTypeResponse(
    val id: UUID,
    val code: String,
    val name: String,
    val category: FermentCategory,
    val description: String?,
    val defaultDurationDays: Int,
    val targetPhMin: BigDecimal?,
    val targetPhMax: BigDecimal?,
    val overFermentationPh: BigDecimal?,
    val targetTempMinC: BigDecimal?,
    val targetTempMaxC: BigDecimal?,
    val accentColor: String?
)
