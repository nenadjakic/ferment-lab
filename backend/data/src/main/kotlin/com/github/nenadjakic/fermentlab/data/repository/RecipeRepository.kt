package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.recipe.Recipe
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecipeRepository : JpaRepository<Recipe, UUID> {

    fun findAllByOrderByNameAsc(): List<Recipe>

    fun findAllByFermentTypeIdOrderByNameAsc(fermentTypeId: UUID): List<Recipe>

    fun findAllByFeaturedTrueOrderByNameAsc(): List<Recipe>
}
