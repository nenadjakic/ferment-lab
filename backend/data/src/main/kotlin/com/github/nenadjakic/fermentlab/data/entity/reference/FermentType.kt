package com.github.nenadjakic.fermentlab.data.entity.reference

import com.github.nenadjakic.fermentlab.data.enum.FermentCategory
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

/**
 * Reference data describing a style of ferment (kombucha, kimchi, sourdough, ...).
 *
 * The target ranges stored here are the baseline used by the analytics and by the
 * over-fermentation detection when a batch is not linked to a recipe.
 */
@Entity
@Table(name = "ferment_types")
data class FermentType(

    /**
     * The primary key identifier for the ferment type.
     */
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    /**
     * Stable machine readable code, e.g. `KOMBUCHA`.
     */
    @Column(name = "code", length = 40, nullable = false, unique = true)
    val code: String,

    /**
     * Human readable name shown in the UI.
     */
    @Column(name = "name", length = 100, nullable = false)
    val name: String,

    /**
     * Grouping used for filtering and colour coding.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30, nullable = false)
    val category: FermentCategory,

    /**
     * Short explanation of the style, surfaced in the guides section.
     */
    @Column(name = "description", length = 1000)
    val description: String? = null,

    /**
     * Typical number of days the ferment needs before it is ready.
     */
    @Column(name = "default_duration_days", nullable = false)
    val defaultDurationDays: Int,

    /**
     * Lower bound of the healthy pH window.
     */
    @Column(name = "target_ph_min", precision = 4, scale = 2)
    val targetPhMin: BigDecimal? = null,

    /**
     * Upper bound of the healthy pH window.
     */
    @Column(name = "target_ph_max", precision = 4, scale = 2)
    val targetPhMax: BigDecimal? = null,

    /**
     * pH below which the batch is considered over-fermented.
     */
    @Column(name = "over_fermentation_ph", precision = 4, scale = 2)
    val overFermentationPh: BigDecimal? = null,

    /**
     * Lower bound of the recommended ambient temperature in degrees Celsius.
     */
    @Column(name = "target_temp_min_c", precision = 5, scale = 2)
    val targetTempMinC: BigDecimal? = null,

    /**
     * Upper bound of the recommended ambient temperature in degrees Celsius.
     */
    @Column(name = "target_temp_max_c", precision = 5, scale = 2)
    val targetTempMaxC: BigDecimal? = null,

    /**
     * Tailwind friendly accent colour token used by the frontend.
     */
    @Column(name = "accent_color", length = 16)
    val accentColor: String? = null
) {
    @PrePersist
    fun prePersist() {
        if (id == null) {
            id = UUID.randomUUID()
        }
    }
}
