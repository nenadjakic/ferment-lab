package com.github.nenadjakic.fermentlab.data.entity.culture

import jakarta.persistence.Column
import jakarta.persistence.Entity
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
 * A single feeding of a culture, forming the culture care log.
 */
@Entity
@Table(name = "culture_feedings")
data class CultureFeeding(

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    /**
     * The culture that was fed.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "culture_id", nullable = false)
    val culture: Culture,

    /**
     * When the feeding happened.
     */
    @Column(name = "fed_at", nullable = false)
    val fedAt: OffsetDateTime = OffsetDateTime.now(),

    /**
     * Amount of feed given, expressed in [unit].
     */
    @Column(name = "amount", precision = 10, scale = 3)
    val amount: BigDecimal? = null,

    /**
     * Unit of [amount], e.g. `g` or `ml`.
     */
    @Column(name = "unit", length = 10)
    val unit: String? = null,

    /**
     * Observation recorded while feeding.
     */
    @Column(name = "note", length = 500)
    val note: String? = null
) {
    @PrePersist
    fun prePersist() {
        if (id == null) {
            id = UUID.randomUUID()
        }
    }
}
