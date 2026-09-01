package com.github.nenadjakic.fermentlab.data.entity.batch

import com.github.nenadjakic.fermentlab.data.enum.MetricType
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
 * A timestamped reading taken from a batch. Measurements are the raw material for the
 * scientific charts and for the batch comparison view.
 */
@Entity
@Table(name = "measurements")
data class Measurement(

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    val batch: Batch,

    /**
     * What was measured.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "metric", length = 30, nullable = false)
    val metric: MetricType,

    /**
     * The reading itself.
     */
    @Column(name = "metric_value", precision = 12, scale = 4, nullable = false)
    val value: BigDecimal,

    /**
     * Unit the reading was taken in; defaults to [MetricType.defaultUnit].
     */
    @Column(name = "unit", length = 15, nullable = false)
    val unit: String,

    /**
     * When the reading was taken.
     */
    @Column(name = "recorded_at", nullable = false)
    val recordedAt: OffsetDateTime = OffsetDateTime.now(),

    /**
     * Optional remark, e.g. "measured after stirring".
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
