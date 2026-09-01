package com.github.nenadjakic.fermentlab.restapp.handler

import java.time.OffsetDateTime

/**
 * Uniform error body returned by every failing endpoint.
 */
data class ErrorInfo(
    val status: Int,
    val error: String,
    val message: String,
    val path: String?,

    /**
     * Field level problems, keyed by property path. Empty for anything other than validation errors.
     */
    val fieldErrors: Map<String, String> = emptyMap(),

    val timestamp: OffsetDateTime = OffsetDateTime.now()
)
