package com.github.nenadjakic.fermentlab.data.entity.culture

import com.github.nenadjakic.fermentlab.data.entity.reference.FermentType
import com.github.nenadjakic.fermentlab.data.enum.CultureHealth
import com.github.nenadjakic.fermentlab.data.enum.CultureKind
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
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

/**
 * A living culture kept in the culture library: a SCOBY, sourdough starter, kefir grains, ...
 *
 * Cultures form a family tree through [parent]: splitting a starter creates a child culture whose
 * [generation] is one higher than its parent.
 */
@Entity
@Table(name = "cultures")
data class Culture(

    /**
     * The primary key identifier for the culture.
     */
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    /**
     * Nickname of the culture, e.g. "Bubbles the SCOBY".
     */
    @Column(name = "name", length = 120, nullable = false)
    var name: String,

    /**
     * The style of ferment this culture is used for.
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "ferment_type_id", nullable = false)
    var fermentType: FermentType,

    /**
     * What kind of culture this is.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "kind", length = 30, nullable = false)
    var kind: CultureKind,

    /**
     * Culture this one was split off from, `null` for a culture that entered the library directly.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_culture_id")
    var parent: Culture? = null,

    /**
     * Distance from the root of the family tree; a directly acquired culture is generation 1.
     */
    @Column(name = "generation", nullable = false)
    var generation: Int = 1,

    /**
     * Date the culture entered the library.
     */
    @Column(name = "acquired_on", nullable = false)
    var acquiredOn: LocalDate = LocalDate.now(),

    /**
     * Self reported vitality.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "health", length = 20, nullable = false)
    var health: CultureHealth = CultureHealth.HEALTHY,

    /**
     * How often the culture wants feeding, in days. `null` for cultures that are not fed.
     */
    @Column(name = "feed_interval_days")
    var feedIntervalDays: Int? = null,

    /**
     * Date of the most recent feeding, kept denormalised so reminders stay a single query.
     */
    @Column(name = "last_fed_on")
    var lastFedOn: LocalDate? = null,

    /**
     * Free text tasting/aroma notes tracked across generations.
     */
    @Column(name = "flavor_profile", length = 255)
    var flavorProfile: String? = null,

    /**
     * Longer form notes about origin and handling.
     */
    @Column(name = "notes", length = 2000)
    var notes: String? = null,

    /**
     * `false` once the culture is retired or lost.
     */
    @Column(name = "active", nullable = false)
    var active: Boolean = true,

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
     * Date the next feeding is due, or `null` when the culture does not need feeding.
     */
    val nextFeedDueOn: LocalDate?
        get() = feedIntervalDays?.let { interval ->
            (lastFedOn ?: acquiredOn).plusDays(interval.toLong())
        }
}
