package com.github.nenadjakic.fermentlab.service

import com.github.nenadjakic.fermentlab.common.dto.alert.AlertResponse
import com.github.nenadjakic.fermentlab.common.exception.ResourceNotFoundException
import com.github.nenadjakic.fermentlab.common.extension.toResponse
import com.github.nenadjakic.fermentlab.data.entity.alert.Alert
import com.github.nenadjakic.fermentlab.data.entity.batch.Batch
import com.github.nenadjakic.fermentlab.data.entity.culture.Culture
import com.github.nenadjakic.fermentlab.data.enum.AlertSeverity
import com.github.nenadjakic.fermentlab.data.enum.AlertType
import com.github.nenadjakic.fermentlab.data.repository.AlertRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

/**
 * The notification centre.
 *
 * Alerts are written by the scheduler through [raise], which is idempotent: the same condition
 * evaluated again updates the existing alert instead of adding a duplicate.
 */
@Service
@Transactional(readOnly = true)
class AlertService(
    private val alertRepository: AlertRepository
) {

    fun findAll(openOnly: Boolean = true): List<AlertResponse> {
        val alerts = if (openOnly) {
            alertRepository.findAllByAcknowledgedAtIsNullOrderByDueAtAsc()
        } else {
            alertRepository.findAllByOrderByDueAtDesc()
        }

        return alerts.sortedWith(SEVERITY_THEN_DUE).map { it.toResponse() }
    }

    fun countOpen(): Long =
        alertRepository.countByAcknowledgedAtIsNull()

    fun findOpenEntities(): List<Alert> =
        alertRepository.findAllByAcknowledgedAtIsNullOrderByDueAtAsc()

    @Transactional
    fun acknowledge(id: UUID): AlertResponse {
        val alert = alertRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("Alert", id) }

        if (alert.acknowledgedAt == null) {
            alert.acknowledgedAt = OffsetDateTime.now()
            alertRepository.save(alert)
        }

        return alert.toResponse()
    }

    @Transactional
    fun acknowledgeAll(): Int {
        val open = alertRepository.findAllByAcknowledgedAtIsNullOrderByDueAtAsc()
        val now = OffsetDateTime.now()

        open.forEach { it.acknowledgedAt = now }
        alertRepository.saveAll(open)

        return open.size
    }

    @Transactional
    fun delete(id: UUID) {
        val alert = alertRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("Alert", id) }

        alertRepository.delete(alert)
    }

    /**
     * Records an alert for a batch, or refreshes the existing one for the same condition.
     *
     * @param dedupeKey deterministic fingerprint of the condition. Re-running the evaluation with the
     * same key updates the message, severity and due date rather than creating a second alert, and
     * leaves an acknowledgement in place.
     */
    @Transactional
    fun raiseForBatch(
        batch: Batch,
        alertType: AlertType,
        severity: AlertSeverity,
        message: String,
        dueAt: OffsetDateTime,
        dedupeKey: String
    ): Alert = raise(dedupeKey, alertType, severity, message, dueAt, batch = batch)

    /**
     * Records an alert for a culture, or refreshes the existing one for the same condition.
     */
    @Transactional
    fun raiseForCulture(
        culture: Culture,
        alertType: AlertType,
        severity: AlertSeverity,
        message: String,
        dueAt: OffsetDateTime,
        dedupeKey: String
    ): Alert = raise(dedupeKey, alertType, severity, message, dueAt, culture = culture)

    /**
     * Drops acknowledged alerts older than [retentionDays] so the table does not grow without bound.
     *
     * @return the number of alerts removed.
     */
    @Transactional
    fun purgeAcknowledged(retentionDays: Long): Int {
        val cutoff = OffsetDateTime.now().minusDays(retentionDays)
        val stale = alertRepository.findAllByOrderByDueAtDesc()
            .filter { it.acknowledgedAt != null && it.createdAt.isBefore(cutoff) }

        alertRepository.deleteAll(stale)
        return stale.size
    }

    private fun raise(
        dedupeKey: String,
        alertType: AlertType,
        severity: AlertSeverity,
        message: String,
        dueAt: OffsetDateTime,
        batch: Batch? = null,
        culture: Culture? = null
    ): Alert {
        val existing = alertRepository.findByDedupeKey(dedupeKey)

        val alert = existing?.apply {
            this.severity = severity
            this.message = message
            this.dueAt = dueAt
        } ?: Alert(
            batch = batch,
            culture = culture,
            alertType = alertType,
            severity = severity,
            message = message,
            dueAt = dueAt,
            dedupeKey = dedupeKey
        )

        return alertRepository.save(alert)
    }

    private companion object {

        /**
         * Most urgent first, then soonest due.
         */
        val SEVERITY_THEN_DUE: Comparator<Alert> =
            compareByDescending<Alert> { it.severity.ordinal }.thenBy { it.dueAt }
    }
}
