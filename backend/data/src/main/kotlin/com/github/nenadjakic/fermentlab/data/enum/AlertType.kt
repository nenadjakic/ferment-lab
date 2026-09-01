package com.github.nenadjakic.fermentlab.data.enum

/**
 * Reason an alert was raised by the scheduler.
 */
enum class AlertType {
    MILESTONE_DUE,
    STAGE_OVERDUE,
    HARVEST_WINDOW,
    OVER_FERMENTATION_RISK,
    TEMPERATURE_OUT_OF_RANGE,
    MEASUREMENT_STALE,
    CULTURE_FEED_DUE
}
