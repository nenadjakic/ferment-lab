package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.batch.BatchStage
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BatchStageRepository : JpaRepository<BatchStage, UUID> {

    fun findAllByBatchIdOrderByPositionAsc(batchId: UUID): List<BatchStage>

    fun findAllByBatchIdInOrderByPositionAsc(batchIds: Collection<UUID>): List<BatchStage>

    fun deleteAllByBatchId(batchId: UUID)
}
