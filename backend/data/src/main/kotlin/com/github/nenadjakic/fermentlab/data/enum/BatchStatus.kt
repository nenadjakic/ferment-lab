package com.github.nenadjakic.fermentlab.data.enum

/**
 * Lifecycle state of a fermentation batch.
 */
enum class BatchStatus {
    PLANNED,
    ACTIVE,
    PAUSED,
    COMPLETED,
    DISCARDED,
    ARCHIVED;

    val isOpen: Boolean
        get() = this == PLANNED || this == ACTIVE || this == PAUSED
}
