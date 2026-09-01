package com.github.nenadjakic.fermentlab.data.enum

/**
 * Kind of entry recorded on the batch journal timeline.
 */
enum class BatchEventType {
    OBSERVATION,
    TASTING,
    FEEDING,
    BURPING,
    STIRRING,
    STAGE_STARTED,
    STAGE_COMPLETED,
    HARVEST,
    ISSUE
}
