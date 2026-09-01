package com.github.nenadjakic.fermentlab.common.dto.batch

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Payload for starting a batch.
 *
 * When [recipeId] is supplied the recipe's steps are copied onto the batch as its stage timeline and
 * the planned finish is derived from the recipe duration. Otherwise the ferment type's default
 * duration is used. An explicit [expectedEndOn] always wins.
 */
data class BatchAddRequest(

    @field:NotBlank
    @field:Size(max = 150)
    val name: String,

    @field:NotNull
    val fermentTypeId: UUID,

    val recipeId: UUID? = null,

    val cultureId: UUID? = null,

    val startedOn: LocalDate? = null,

    val expectedEndOn: LocalDate? = null,

    @field:Size(max = 100)
    val container: String? = null,

    @field:Positive
    val batchSize: BigDecimal? = null,

    @field:Size(max = 10)
    val batchSizeUnit: String? = null,

    @field:Size(max = 100)
    val location: String? = null,

    @field:Size(max = 2000)
    val notes: String? = null
)
