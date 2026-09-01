package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.batch.BatchEvent
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BatchEventRepository : JpaRepository<BatchEvent, UUID> {

    fun findAllByBatchIdOrderByOccurredAtDesc(batchId: UUID): List<BatchEvent>

    /**
     * Latest journal entries across every batch, for the dashboard activity feed.
     */
    fun findTop12ByOrderByOccurredAtDesc(): List<BatchEvent>

    fun deleteAllByBatchId(batchId: UUID)
}
