package com.github.nenadjakic.fermentlab.data.entity.batch

import com.github.nenadjakic.fermentlab.data.enum.BatchEventType
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
import java.time.OffsetDateTime
import java.util.UUID

/**
 * A journal entry on a batch: an observation, a tasting note, a burp, a stage transition.
 */
@Entity
@Table(name = "batch_events")
data class BatchEvent(

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    val batch: Batch,

    /**
     * Kind of entry.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 30, nullable = false)
    val eventType: BatchEventType,

    /**
     * When it happened.
     */
    @Column(name = "occurred_at", nullable = false)
    val occurredAt: OffsetDateTime = OffsetDateTime.now(),

    /**
     * Headline of the entry.
     */
    @Column(name = "title", length = 150, nullable = false)
    val title: String,

    /**
     * Body of the entry.
     */
    @Column(name = "description", length = 2000)
    val description: String? = null
) {
    @PrePersist
    fun prePersist() {
        if (id == null) {
            id = UUID.randomUUID()
        }
    }
}
