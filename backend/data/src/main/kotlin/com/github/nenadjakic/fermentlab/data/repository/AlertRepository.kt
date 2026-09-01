package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.alert.Alert
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AlertRepository : JpaRepository<Alert, UUID> {

    fun findByDedupeKey(dedupeKey: String): Alert?

    fun findAllByAcknowledgedAtIsNullOrderByDueAtAsc(): List<Alert>

    fun findAllByOrderByDueAtDesc(): List<Alert>

    fun findAllByBatchIdOrderByDueAtDesc(batchId: UUID): List<Alert>

    fun countByAcknowledgedAtIsNull(): Long

    fun deleteAllByBatchId(batchId: UUID)
}
