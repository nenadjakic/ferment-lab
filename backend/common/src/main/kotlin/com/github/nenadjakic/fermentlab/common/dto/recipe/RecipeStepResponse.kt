package com.github.nenadjakic.fermentlab.common.dto.recipe

import com.github.nenadjakic.fermentlab.data.enum.StagePhase
import java.util.UUID

/**
 * One instruction of a recipe's method.
 */
data class RecipeStepResponse(
    val id: UUID,
    val position: Int,
    val title: String,
    val instruction: String,
    val phase: StagePhase,
    val durationDays: Int
)
