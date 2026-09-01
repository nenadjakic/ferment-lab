package com.github.nenadjakic.fermentlab.common.dto.alert

import com.github.nenadjakic.fermentlab.data.enum.AlertSeverity
import com.github.nenadjakic.fermentlab.data.enum.AlertType
import java.time.OffsetDateTime
import java.util.UUID

/**
 * A notification from the notification centre.
 */
data class AlertResponse(
    val id: UUID,
    val batchId: UUID?,
    val batchCode: String?,
    val batchName: String?,
    val cultureId: UUID?,
    val cultureName: String?,
    val alertType: AlertType,
    val severity: AlertSeverity,
    val message: String,
    val dueAt: OffsetDateTime,
    val acknowledgedAt: OffsetDateTime?,
    val createdAt: OffsetDateTime
)
