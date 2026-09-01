package com.github.nenadjakic.fermentlab.common.dto.culture

import com.github.nenadjakic.fermentlab.common.dto.reference.FermentTypeSimpleResponse
import com.github.nenadjakic.fermentlab.data.enum.CultureHealth
import com.github.nenadjakic.fermentlab.data.enum.CultureKind
import java.time.LocalDate
import java.util.UUID

/**
 * A culture from the library together with its derived feeding schedule.
 */
data class CultureResponse(
    val id: UUID,
    val name: String,
    val fermentType: FermentTypeSimpleResponse,
    val kind: CultureKind,
    val parentId: UUID?,
    val parentName: String?,
    val generation: Int,
    val acquiredOn: LocalDate,
    val health: CultureHealth,
    val feedIntervalDays: Int?,
    val lastFedOn: LocalDate?,
    val nextFeedDueOn: LocalDate?,

    /** `true` when [nextFeedDueOn] is today or in the past. */
    val feedDue: Boolean,

    val flavorProfile: String?,
    val notes: String?,
    val active: Boolean,

    /** Number of cultures split directly off this one. */
    val childCount: Int
)
