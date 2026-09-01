package com.github.nenadjakic.fermentlab.common.enum

/**
 * Derived state of a running batch, computed from its schedule and its latest readings.
 *
 * This is not persisted: it is recalculated on every read so it always reflects the newest
 * measurement.
 */
enum class BatchHealth {

    /** Not started yet. */
    PLANNED,

    /** Inside its schedule and inside the healthy windows for the style. */
    ON_TRACK,

    /** Within a day of the planned finish; time to taste and decide. */
    READY,

    /** Something is drifting: temperature outside the window, or no readings for a while. */
    ATTENTION,

    /** Past the sour limit for the style, or well past the planned finish. */
    OVER_FERMENTED,

    /** Batch is closed. */
    CLOSED
}
