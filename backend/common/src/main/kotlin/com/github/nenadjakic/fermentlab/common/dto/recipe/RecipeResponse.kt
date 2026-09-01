package com.github.nenadjakic.fermentlab.common.dto.recipe

import com.github.nenadjakic.fermentlab.common.dto.reference.FermentTypeSimpleResponse
import com.github.nenadjakic.fermentlab.data.enum.Difficulty
import java.math.BigDecimal
import java.util.UUID

/**
 * Recipe card used on the recipe list.
 */
data class RecipeResponse(
    val id: UUID,
    val name: String,
    val fermentType: FermentTypeSimpleResponse,
    val difficulty: Difficulty,
    val summary: String?,
    val totalDurationDays: Int,
    val yieldAmount: BigDecimal?,
    val yieldUnit: String?,
    val salinityPercent: BigDecimal?,
    val hydrationPercent: BigDecimal?,
    val source: String?,
    val featured: Boolean,
    val stepCount: Int,
    val ingredientCount: Int
)
