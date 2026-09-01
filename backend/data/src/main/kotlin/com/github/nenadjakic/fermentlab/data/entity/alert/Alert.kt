package com.github.nenadjakic.fermentlab.data.entity.alert

import com.github.nenadjakic.fermentlab.data.entity.batch.Batch
import com.github.nenadjakic.fermentlab.data.entity.culture.Culture
import com.github.nenadjakic.fermentlab.data.enum.AlertSeverity
import com.github.nenadjakic.fermentlab.data.enum.AlertType
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
 * A notification raised by the scheduler: a milestone falling due, a batch drifting past its
 * harvest window, a culture that needs feeding.
 *
 * [dedupeKey] carries a unique, deterministic fingerprint of the condition that produced the alert
 * so repeated scheduler runs update rather than duplicate an existing alert.
 */
@Entity
@Table(name = "alerts")
data class Alert(

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    /**
     * Batch the alert is about, `null` for culture level alerts.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id")
    var batch: Batch? = null,

    /**
     * Culture the alert is about, `null` for batch level alerts.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "culture_id")
    var culture: Culture? = null,

    /**
     * Why the alert was raised.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", length = 40, nullable = false)
    var alertType: AlertType,

    /**
     * Urgency.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 10, nullable = false)
    var severity: AlertSeverity = AlertSeverity.INFO,

    /**
     * Message shown to the user.
     */
    @Column(name = "message", length = 500, nullable = false)
    var message: String,

    /**
     * Moment the alert becomes actionable.
     */
    @Column(name = "due_at", nullable = false)
    var dueAt: OffsetDateTime = OffsetDateTime.now(),

    /**
     * Deterministic fingerprint used to avoid duplicate alerts across scheduler runs.
     */
    @Column(name = "dedupe_key", length = 200, nullable = false, unique = true)
    var dedupeKey: String,

    /**
     * Set once the user dismisses the alert.
     */
    @Column(name = "acknowledged_at")
    var acknowledgedAt: OffsetDateTime? = null,

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
