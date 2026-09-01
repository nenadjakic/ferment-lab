package com.github.nenadjakic.fermentlab.common.dto.culture

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Payload for splitting a culture, which creates a child one generation down the family tree.
 */
data class CultureSplitRequest(

    @field:NotBlank
    @field:Size(max = 120)
    val name: String,

    @field:Size(max = 2000)
    val notes: String? = null
)
