package com.github.nenadjakic.fermentlab.common.dto.recipe

import java.math.BigDecimal
import java.util.UUID

/**
 * One line of a recipe's ingredient list.
 */
data class RecipeIngredientResponse(
    val id: UUID,
    val position: Int,
    val name: String,
    val quantity: BigDecimal?,
    val unit: String?,
    val note: String?
)
