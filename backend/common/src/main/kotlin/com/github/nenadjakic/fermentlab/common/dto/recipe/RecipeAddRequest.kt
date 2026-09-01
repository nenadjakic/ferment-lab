package com.github.nenadjakic.fermentlab.common.dto.recipe

import com.github.nenadjakic.fermentlab.data.enum.Difficulty
import com.github.nenadjakic.fermentlab.data.enum.StagePhase
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.UUID

/**
 * Payload for creating a recipe together with its ingredients and method.
 *
 * The total duration is derived from the sum of the step durations, so it is not accepted here.
 */
data class RecipeAddRequest(

    @field:NotBlank
    @field:Size(max = 150)
    val name: String,

    @field:NotNull
    val fermentTypeId: UUID,

    @field:NotNull
    val difficulty: Difficulty = Difficulty.BEGINNER,

    @field:Size(max = 1000)
    val summary: String? = null,

    val yieldAmount: BigDecimal? = null,

    @field:Size(max = 10)
    val yieldUnit: String? = null,

    val salinityPercent: BigDecimal? = null,

    val hydrationPercent: BigDecimal? = null,

    @field:Size(max = 150)
    val source: String? = null,

    val featured: Boolean = false,

    @field:Valid
    val ingredients: List<IngredientRequest> = emptyList(),

    @field:Valid
    @field:NotEmpty
    val steps: List<StepRequest> = emptyList()
) {

    /**
     * One ingredient line. Ordering follows the position in the list.
     */
    data class IngredientRequest(

        @field:NotBlank
        @field:Size(max = 120)
        val name: String,

        val quantity: BigDecimal? = null,

        @field:Size(max = 15)
        val unit: String? = null,

        @field:Size(max = 255)
        val note: String? = null
    )

    /**
     * One method step. Ordering follows the position in the list.
     */
    data class StepRequest(

        @field:NotBlank
        @field:Size(max = 150)
        val title: String,

        @field:NotBlank
        @field:Size(max = 2000)
        val instruction: String,

        @field:NotNull
        val phase: StagePhase = StagePhase.PRIMARY,

        @field:PositiveOrZero
        val durationDays: Int = 0
    )
}
