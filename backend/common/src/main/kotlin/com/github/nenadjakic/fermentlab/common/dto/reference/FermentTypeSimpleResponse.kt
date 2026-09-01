package com.github.nenadjakic.fermentlab.common.dto.reference

import com.github.nenadjakic.fermentlab.data.enum.FermentCategory
import java.util.UUID

/**
 * Lightweight ferment type used when a record only needs to label and colour code its style.
 */
data class FermentTypeSimpleResponse(
    val id: UUID,
    val code: String,
    val name: String,
    val category: FermentCategory,
    val accentColor: String?
)
