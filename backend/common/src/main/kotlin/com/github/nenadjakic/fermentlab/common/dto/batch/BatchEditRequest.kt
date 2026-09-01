package com.github.nenadjakic.fermentlab.common.dto.batch

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Payload for editing a batch. The ferment type and the recipe are fixed once a batch exists,
 * because the stage timeline was generated from them.
 */
data class BatchEditRequest(

    @field:NotBlank
    @field:Size(max = 150)
    val name: String,

    @field:NotNull
    val expectedEndOn: LocalDate,

    val cultureId: UUID? = null,

    @field:Size(max = 100)
    val container: String? = null,

    @field:Positive
    val batchSize: BigDecimal? = null,

    @field:Size(max = 10)
    val batchSizeUnit: String? = null,

    @field:Size(max = 100)
    val location: String? = null,

    @field:Min(1)
    @field:Max(5)
    val rating: Int? = null,

    @field:Size(max = 2000)
    val notes: String? = null
)
