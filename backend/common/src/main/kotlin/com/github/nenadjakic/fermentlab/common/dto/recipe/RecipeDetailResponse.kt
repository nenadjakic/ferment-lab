package com.github.nenadjakic.fermentlab.common.dto.recipe

/**
 * Full recipe, including the ingredient list and the method that becomes a batch's stage template.
 */
data class RecipeDetailResponse(
    val recipe: RecipeResponse,
    val ingredients: List<RecipeIngredientResponse>,
    val steps: List<RecipeStepResponse>
)
