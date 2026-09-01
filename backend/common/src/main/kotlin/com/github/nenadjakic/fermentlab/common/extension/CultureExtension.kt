package com.github.nenadjakic.fermentlab.common.extension

import com.github.nenadjakic.fermentlab.common.dto.culture.CultureFeedingResponse
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureResponse
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureTreeNodeResponse
import com.github.nenadjakic.fermentlab.data.entity.culture.Culture
import com.github.nenadjakic.fermentlab.data.entity.culture.CultureFeeding
import java.time.LocalDate

fun Culture.toResponse(childCount: Int = 0): CultureResponse {
    val nextFeed = nextFeedDueOn
    return CultureResponse(
        id = requireNotNull(id),
        name = name,
        fermentType = fermentType.toSimpleResponse(),
        kind = kind,
        parentId = parent?.id,
        parentName = parent?.name,
        generation = generation,
        acquiredOn = acquiredOn,
        health = health,
        feedIntervalDays = feedIntervalDays,
        lastFedOn = lastFedOn,
        nextFeedDueOn = nextFeed,
        feedDue = nextFeed != null && !nextFeed.isAfter(LocalDate.now()),
        flavorProfile = flavorProfile,
        notes = notes,
        active = active,
        childCount = childCount
    )
}

fun CultureFeeding.toResponse(): CultureFeedingResponse =
    CultureFeedingResponse(
        id = requireNotNull(id),
        cultureId = requireNotNull(culture.id),
        fedAt = fedAt,
        amount = amount,
        unit = unit,
        note = note
    )

/**
 * Folds a flat culture list into the family trees it describes.
 *
 * Cultures whose parent is not part of [this] become roots, so the function is safe to call on a
 * filtered subset of the library.
 */
fun Collection<Culture>.toForest(): List<CultureTreeNodeResponse> {
    val ids = mapNotNull { it.id }.toSet()
    val childrenByParent = groupBy { it.parent?.id?.takeIf(ids::contains) }

    fun build(culture: Culture): CultureTreeNodeResponse =
        CultureTreeNodeResponse(
            id = requireNotNull(culture.id),
            name = culture.name,
            kind = culture.kind,
            health = culture.health,
            generation = culture.generation,
            acquiredOn = culture.acquiredOn,
            flavorProfile = culture.flavorProfile,
            children = childrenByParent[culture.id]
                .orEmpty()
                .sortedBy { it.acquiredOn }
                .map(::build)
        )

    return childrenByParent[null]
        .orEmpty()
        .sortedBy { it.name }
        .map(::build)
}
