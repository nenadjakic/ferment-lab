package com.github.nenadjakic.fermentlab.common.dto.culture

import com.github.nenadjakic.fermentlab.data.enum.CultureHealth
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

/**
 * Payload for editing a culture. Lineage and ferment type are immutable once set.
 */
data class CultureUpdateRequest(

    @field:NotBlank
    @field:Size(max = 120)
    val name: String,

    @field:NotNull
    val health: CultureHealth,

    @field:Positive
    val feedIntervalDays: Int? = null,

    @field:Size(max = 255)
    val flavorProfile: String? = null,

    @field:Size(max = 2000)
    val notes: String? = null,

    val active: Boolean = true
)
