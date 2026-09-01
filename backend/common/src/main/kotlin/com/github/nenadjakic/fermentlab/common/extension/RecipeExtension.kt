package com.github.nenadjakic.fermentlab.common.extension

import com.github.nenadjakic.fermentlab.common.dto.recipe.RecipeIngredientResponse
import com.github.nenadjakic.fermentlab.common.dto.recipe.RecipeResponse
import com.github.nenadjakic.fermentlab.common.dto.recipe.RecipeStepResponse
import com.github.nenadjakic.fermentlab.data.entity.recipe.Recipe
import com.github.nenadjakic.fermentlab.data.entity.recipe.RecipeIngredient
import com.github.nenadjakic.fermentlab.data.entity.recipe.RecipeStep

fun Recipe.toResponse(stepCount: Int = 0, ingredientCount: Int = 0): RecipeResponse =
    RecipeResponse(
        id = requireNotNull(id),
        name = name,
        fermentType = fermentType.toSimpleResponse(),
        difficulty = difficulty,
        summary = summary,
        totalDurationDays = totalDurationDays,
        yieldAmount = yieldAmount,
        yieldUnit = yieldUnit,
        salinityPercent = salinityPercent,
        hydrationPercent = hydrationPercent,
        source = source,
        featured = featured,
        stepCount = stepCount,
        ingredientCount = ingredientCount
    )

fun RecipeIngredient.toResponse(): RecipeIngredientResponse =
    RecipeIngredientResponse(
        id = requireNotNull(id),
        position = position,
        name = name,
        quantity = quantity,
        unit = unit,
        note = note
    )

fun RecipeStep.toResponse(): RecipeStepResponse =
    RecipeStepResponse(
        id = requireNotNull(id),
        position = position,
        title = title,
        instruction = instruction,
        phase = phase,
        durationDays = durationDays
    )
