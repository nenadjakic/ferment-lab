package com.github.nenadjakic.fermentlab.data.entity.recipe

import com.github.nenadjakic.fermentlab.data.entity.reference.FermentType
import com.github.nenadjakic.fermentlab.data.enum.Difficulty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

/**
 * A curated, step by step fermentation method that batches can be started from.
 */
@Entity
@Table(name = "recipes")
data class Recipe(

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    /**
     * Recipe title.
     */
    @Column(name = "name", length = 150, nullable = false)
    var name: String,

    /**
     * Style of ferment the recipe produces.
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "ferment_type_id", nullable = false)
    var fermentType: FermentType,

    /**
     * Skill level required.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 20, nullable = false)
    var difficulty: Difficulty = Difficulty.BEGINNER,

    /**
     * One paragraph pitch shown on the recipe card.
     */
    @Column(name = "summary", length = 1000)
    var summary: String? = null,

    /**
     * Total number of days from start to ready, summed across all steps.
     */
    @Column(name = "total_duration_days", nullable = false)
    var totalDurationDays: Int,

    /**
     * Expected yield in litres (or kilograms for solid ferments).
     */
    @Column(name = "yield_amount", precision = 8, scale = 3)
    var yieldAmount: BigDecimal? = null,

    /**
     * Unit of [yieldAmount].
     */
    @Column(name = "yield_unit", length = 10)
    var yieldUnit: String? = null,

    /**
     * Brine strength for vegetable ferments, expressed as a percentage of the total weight.
     */
    @Column(name = "salinity_percent", precision = 5, scale = 2)
    var salinityPercent: BigDecimal? = null,

    /**
     * Baker's hydration for dough ferments, expressed as a percentage of flour weight.
     */
    @Column(name = "hydration_percent", precision = 5, scale = 2)
    var hydrationPercent: BigDecimal? = null,

    /**
     * Where the method came from.
     */
    @Column(name = "source", length = 150)
    var source: String? = null,

    /**
     * Highlighted on the recipes landing page when `true`.
     */
    @Column(name = "featured", nullable = false)
    var featured: Boolean = false,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        if (id == null) {
            id = UUID.randomUUID()
        }
        createdAt = OffsetDateTime.now()
    }
}
