package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.recipe.RecipeIngredient
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecipeIngredientRepository : JpaRepository<RecipeIngredient, UUID> {

    fun findAllByRecipeIdOrderByPositionAsc(recipeId: UUID): List<RecipeIngredient>
}
