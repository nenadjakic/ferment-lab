package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.recipe.RecipeStep
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecipeStepRepository : JpaRepository<RecipeStep, UUID> {

    fun findAllByRecipeIdOrderByPositionAsc(recipeId: UUID): List<RecipeStep>
}
