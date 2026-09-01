package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.batch.Measurement
import com.github.nenadjakic.fermentlab.data.enum.MetricType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface MeasurementRepository : JpaRepository<Measurement, UUID> {

    fun findAllByBatchIdOrderByRecordedAtAsc(batchId: UUID): List<Measurement>

    fun findAllByBatchIdAndMetricOrderByRecordedAtAsc(batchId: UUID, metric: MetricType): List<Measurement>

    fun findAllByBatchIdInOrderByRecordedAtAsc(batchIds: Collection<UUID>): List<Measurement>

    fun countByBatchId(batchId: UUID): Long

    /**
     * The most recent reading of every metric for the given batches.
     *
     * Used to render the "current state" tiles and to feed the over-fermentation detection without
     * pulling a batch's whole measurement history.
     */
    @Query(
        """
        SELECT m FROM Measurement m
        WHERE m.batch.id IN :batchIds
          AND m.recordedAt = (
                SELECT MAX(latest.recordedAt) FROM Measurement latest
                WHERE latest.batch.id = m.batch.id AND latest.metric = m.metric
          )
        """
    )
    fun findLatestPerMetric(@Param("batchIds") batchIds: Collection<UUID>): List<Measurement>

    fun deleteAllByBatchId(batchId: UUID)
}
