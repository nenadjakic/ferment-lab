package com.github.nenadjakic.fermentlab.data.entity.batch

import com.github.nenadjakic.fermentlab.data.enum.StagePhase
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
 * One phase of a batch's timeline, e.g. primary ferment, second ferment, cold conditioning.
 *
 * Stages carry their own planned duration so the frontend can render per-phase countdowns.
 */
@Entity
@Table(name = "batch_stages")
data class BatchStage(

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    val batch: Batch,

    /**
     * Ordering within the timeline, starting at 1.
     */
    @Column(name = "ordinal", nullable = false)
    val position: Int,

    /**
     * Stage label shown on the timeline.
     */
    @Column(name = "name", length = 150, nullable = false)
    var name: String,

    /**
     * Fermentation phase this stage represents.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "phase", length = 20, nullable = false)
    var phase: StagePhase = StagePhase.PRIMARY,

    /**
     * Instruction copied from the recipe step this stage was generated from.
     */
    @Column(name = "instruction", length = 2000)
    var instruction: String? = null,

    /**
     * Planned length of the stage in days.
     */
    @Column(name = "planned_duration_days", nullable = false)
    var plannedDurationDays: Int = 0,

    /**
     * Set when the stage is started; `null` while the stage is still pending.
     */
    @Column(name = "started_at")
    var startedAt: OffsetDateTime? = null,

    /**
     * Set when the stage is finished.
     */
    @Column(name = "completed_at")
    var completedAt: OffsetDateTime? = null,

    /**
     * Note captured while working the stage.
     */
    @Column(name = "note", length = 1000)
    var note: String? = null
) {
    @PrePersist
    fun prePersist() {
        if (id == null) {
            id = UUID.randomUUID()
        }
    }
}
