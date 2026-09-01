package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.recipe.RecipeIngredient
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecipeIngredientRepository : JpaRepository<RecipeIngredient, UUID> {

    fun findAllByRecipeIdOrderByPositionAsc(recipeId: UUID): List<RecipeIngredient>

    /**
     * Clears a recipe's ingredient list. Editing a recipe replaces the whole list rather than
     * diffing rows, so the old lines are removed before the new ones are inserted.
     */
    fun deleteAllByRecipeId(recipeId: UUID)
}
