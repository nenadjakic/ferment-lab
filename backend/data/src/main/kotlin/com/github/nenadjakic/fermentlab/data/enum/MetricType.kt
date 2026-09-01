package com.github.nenadjakic.fermentlab.data.enum

/**
 * Measurable property logged against a batch.
 *
 * @property defaultUnit unit used when the client does not supply one.
 * @property lowerIsMoreFermented `true` when a falling value indicates progressing fermentation
 * (pH and specific gravity), used by the over-fermentation detection.
 */
enum class MetricType(
    val defaultUnit: String,
    val lowerIsMoreFermented: Boolean
) {
    PH("pH", true),
    TEMPERATURE("°C", false),
    BRIX("°Bx", true),
    SPECIFIC_GRAVITY("SG", true),
    SALINITY("%", false),
    TITRATABLE_ACIDITY("g/L", false),
    ABV("%", false),
    WEIGHT("g", false),
    HYDRATION("%", false)
}
