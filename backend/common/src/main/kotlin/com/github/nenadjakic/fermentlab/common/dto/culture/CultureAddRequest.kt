package com.github.nenadjakic.fermentlab.common.dto.culture

import com.github.nenadjakic.fermentlab.data.enum.CultureHealth
import com.github.nenadjakic.fermentlab.data.enum.CultureKind
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.util.UUID

/**
 * Payload for adding a culture to the library.
 */
data class CultureAddRequest(

    @field:NotBlank
    @field:Size(max = 120)
    val name: String,

    @field:NotNull
    val fermentTypeId: UUID,

    @field:NotNull
    val kind: CultureKind,

    /** Culture this one was split off from; sets the generation automatically when supplied. */
    val parentCultureId: UUID? = null,

    val acquiredOn: LocalDate? = null,

    val health: CultureHealth = CultureHealth.HEALTHY,

    @field:Positive
    val feedIntervalDays: Int? = null,

    @field:Size(max = 255)
    val flavorProfile: String? = null,

    @field:Size(max = 2000)
    val notes: String? = null
)
