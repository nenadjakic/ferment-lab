package com.github.nenadjakic.fermentlab.service

import com.github.nenadjakic.fermentlab.common.dto.recipe.RecipeAddRequest
import com.github.nenadjakic.fermentlab.common.dto.recipe.RecipeDetailResponse
import com.github.nenadjakic.fermentlab.common.dto.recipe.RecipeResponse
import com.github.nenadjakic.fermentlab.common.exception.ResourceNotFoundException
import com.github.nenadjakic.fermentlab.common.extension.toResponse
import com.github.nenadjakic.fermentlab.data.entity.recipe.Recipe
import com.github.nenadjakic.fermentlab.data.entity.recipe.RecipeIngredient
import com.github.nenadjakic.fermentlab.data.entity.recipe.RecipeStep
import com.github.nenadjakic.fermentlab.data.repository.RecipeIngredientRepository
import com.github.nenadjakic.fermentlab.data.repository.RecipeRepository
import com.github.nenadjakic.fermentlab.data.repository.RecipeStepRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * The recipe and guide library.
 */
@Service
@Transactional(readOnly = true)
class RecipeService(
    private val recipeRepository: RecipeRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val recipeStepRepository: RecipeStepRepository,
    private val fermentTypeService: FermentTypeService
) {

    fun findAll(fermentTypeId: UUID? = null, featuredOnly: Boolean = false): List<RecipeResponse> {
        val recipes = when {
            fermentTypeId != null -> recipeRepository.findAllByFermentTypeIdOrderByNameAsc(fermentTypeId)
            featuredOnly -> recipeRepository.findAllByFeaturedTrueOrderByNameAsc()
            else -> recipeRepository.findAllByOrderByNameAsc()
        }

        return recipes.map { recipe ->
            val id = requireNotNull(recipe.id)
            recipe.toResponse(
                stepCount = recipeStepRepository.findAllByRecipeIdOrderByPositionAsc(id).size,
                ingredientCount = recipeIngredientRepository.findAllByRecipeIdOrderByPositionAsc(id).size
            )
        }
    }

    fun findById(id: UUID): RecipeDetailResponse {
        val recipe = getEntity(id)
        val ingredients = recipeIngredientRepository.findAllByRecipeIdOrderByPositionAsc(id)
        val steps = recipeStepRepository.findAllByRecipeIdOrderByPositionAsc(id)

        return RecipeDetailResponse(
            recipe = recipe.toResponse(stepCount = steps.size, ingredientCount = ingredients.size),
            ingredients = ingredients.map { it.toResponse() },
            steps = steps.map { it.toResponse() }
        )
    }

    /**
     * Creates a recipe. The total duration is the sum of the step durations, so the schedule of a
     * batch started from this recipe always agrees with its own timeline.
     */
    @Transactional
    fun add(request: RecipeAddRequest): RecipeDetailResponse {
        val totalDuration = request.steps.sumOf { it.durationDays }.coerceAtLeast(1)

        val recipe = recipeRepository.save(
            Recipe(
                name = request.name,
                fermentType = fermentTypeService.getEntity(request.fermentTypeId),
                difficulty = request.difficulty,
                summary = request.summary,
                totalDurationDays = totalDuration,
                yieldAmount = request.yieldAmount,
                yieldUnit = request.yieldUnit,
                salinityPercent = request.salinityPercent,
                hydrationPercent = request.hydrationPercent,
                source = request.source,
                featured = request.featured
            )
        )

        recipeIngredientRepository.saveAll(
            request.ingredients.mapIndexed { index, ingredient ->
                RecipeIngredient(
                    recipe = recipe,
                    position = index + 1,
                    name = ingredient.name,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit,
                    note = ingredient.note
                )
            }
        )

        recipeStepRepository.saveAll(
            request.steps.mapIndexed { index, step ->
                RecipeStep(
                    recipe = recipe,
                    position = index + 1,
                    title = step.title,
                    instruction = step.instruction,
                    phase = step.phase,
                    durationDays = step.durationDays
                )
            }
        )

        return findById(requireNotNull(recipe.id))
    }

    @Transactional
    fun delete(id: UUID) {
        recipeRepository.delete(getEntity(id))
    }

    fun getEntity(id: UUID): Recipe =
        recipeRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("Recipe", id) }

    /**
     * The method of a recipe, used as the stage template when a batch is started from it.
     */
    fun findSteps(recipeId: UUID): List<RecipeStep> =
        recipeStepRepository.findAllByRecipeIdOrderByPositionAsc(recipeId)
}
