package com.github.nenadjakic.fermentlab.data.enum

/**
 * Self reported vitality of a culture, driving the feed reminders and dashboard badges.
 */
enum class CultureHealth {
    THRIVING,
    HEALTHY,
    SLUGGISH,
    DORMANT,
    LOST
}
