package com.github.nenadjakic.fermentlab.common.dto.culture

import com.github.nenadjakic.fermentlab.data.enum.CultureHealth
import com.github.nenadjakic.fermentlab.data.enum.CultureKind
import java.time.LocalDate
import java.util.UUID

/**
 * One node of a culture family tree. Roots are cultures without a parent; [children] recurses down
 * every generation.
 */
data class CultureTreeNodeResponse(
    val id: UUID,
    val name: String,
    val kind: CultureKind,
    val health: CultureHealth,
    val generation: Int,
    val acquiredOn: LocalDate,
    val flavorProfile: String?,
    val children: List<CultureTreeNodeResponse>
)
