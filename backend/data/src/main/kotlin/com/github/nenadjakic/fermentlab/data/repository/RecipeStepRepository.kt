package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.recipe.RecipeStep
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecipeStepRepository : JpaRepository<RecipeStep, UUID> {

    fun findAllByRecipeIdOrderByPositionAsc(recipeId: UUID): List<RecipeStep>

    /**
     * Clears a recipe's method. Editing a recipe replaces the whole method rather than diffing
     * steps, so the old steps are removed before the new ones are inserted.
     */
    fun deleteAllByRecipeId(recipeId: UUID)
}
