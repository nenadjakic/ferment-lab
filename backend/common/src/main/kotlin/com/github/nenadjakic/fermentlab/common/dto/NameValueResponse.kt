package com.github.nenadjakic.fermentlab.common.dto

/**
 * Generic label/number pair used by the dashboard distribution charts.
 */
data class NameValueResponse(
    val name: String,
    val value: Long
)
