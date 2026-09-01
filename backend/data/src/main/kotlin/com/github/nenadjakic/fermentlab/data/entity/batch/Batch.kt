package com.github.nenadjakic.fermentlab.data.entity.batch

import com.github.nenadjakic.fermentlab.data.entity.culture.Culture
import com.github.nenadjakic.fermentlab.data.entity.recipe.Recipe
import com.github.nenadjakic.fermentlab.data.entity.reference.FermentType
import com.github.nenadjakic.fermentlab.data.enum.BatchOutcome
import com.github.nenadjakic.fermentlab.data.enum.BatchStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * A single fermentation run - the central aggregate of the application.
 *
 * A batch always has a [fermentType]; it optionally follows a [recipe] and optionally consumes a
 * [culture] from the culture library.
 */
@Entity
@Table(name = "batches")
data class Batch(

    /**
     * The primary key identifier for the batch.
     */
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    /**
     * Short human friendly reference, e.g. `KOM-0007`.
     */
    @Column(name = "code", length = 20, nullable = false, unique = true)
    var code: String,

    /**
     * Batch name chosen by the fermenter.
     */
    @Column(name = "name", length = 150, nullable = false)
    var name: String,

    /**
     * Style of ferment.
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "ferment_type_id", nullable = false)
    var fermentType: FermentType,

    /**
     * Recipe the batch follows, `null` for a free form batch.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipe_id")
    var recipe: Recipe? = null,

    /**
     * Culture used to inoculate the batch, `null` for wild ferments.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "culture_id")
    var culture: Culture? = null,

    /**
     * Lifecycle state.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    var status: BatchStatus = BatchStatus.ACTIVE,

    /**
     * Day the ferment was pitched.
     */
    @Column(name = "started_on", nullable = false)
    var startedOn: LocalDate = LocalDate.now(),

    /**
     * Day the ferment is expected to be ready; drives the progress ring and milestone alerts.
     */
    @Column(name = "expected_end_on", nullable = false)
    var expectedEndOn: LocalDate,

    /**
     * Day the batch was actually closed.
     */
    @Column(name = "ended_on")
    var endedOn: LocalDate? = null,

    /**
     * Vessel the ferment lives in, e.g. "5 L glass crock".
     */
    @Column(name = "container", length = 100)
    var container: String? = null,

    /**
     * Batch size in litres (or kilograms for solid ferments).
     */
    @Column(name = "batch_size", precision = 8, scale = 3)
    var batchSize: BigDecimal? = null,

    /**
     * Unit of [batchSize].
     */
    @Column(name = "batch_size_unit", length = 10)
    var batchSizeUnit: String? = "L",

    /**
     * Where the vessel is kept, e.g. "pantry shelf".
     */
    @Column(name = "location", length = 100)
    var location: String? = null,

    /**
     * Score from 1 to 5 given once the batch is tasted.
     */
    @Column(name = "rating")
    var rating: Int? = null,

    /**
     * Qualitative result recorded on completion.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 20)
    var outcome: BatchOutcome? = null,

    /**
     * Free form notes about the run.
     */
    @Column(name = "notes", length = 2000)
    var notes: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        if (id == null) {
            id = UUID.randomUUID()
        }
        createdAt = OffsetDateTime.now()
        updatedAt = createdAt
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = OffsetDateTime.now()
    }

    /**
     * Number of days the ferment has been running, capped at the day it was closed.
     */
    val dayCount: Long
        get() = ChronoUnit.DAYS.between(startedOn, endedOn ?: LocalDate.now()).coerceAtLeast(0)

    /**
     * Planned length of the run in days, always at least one to avoid division by zero.
     */
    val plannedDurationDays: Long
        get() = ChronoUnit.DAYS.between(startedOn, expectedEndOn).coerceAtLeast(1)

    /**
     * Completion of the planned schedule as a percentage, not capped so an overdue batch reports
     * more than 100.
     */
    val progressPercent: Int
        get() = (dayCount * 100 / plannedDurationDays).toInt()
}
