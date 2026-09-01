package com.github.nenadjakic.fermentlab.service

import com.github.nenadjakic.fermentlab.common.dto.analytics.MetricSeriesResponse
import com.github.nenadjakic.fermentlab.common.dto.batch.MeasurementAddRequest
import com.github.nenadjakic.fermentlab.common.dto.batch.MeasurementResponse
import com.github.nenadjakic.fermentlab.common.exception.ResourceNotFoundException
import com.github.nenadjakic.fermentlab.common.extension.toResponse
import com.github.nenadjakic.fermentlab.data.entity.batch.Batch
import com.github.nenadjakic.fermentlab.data.entity.batch.Measurement
import com.github.nenadjakic.fermentlab.data.enum.MetricType
import com.github.nenadjakic.fermentlab.data.repository.MeasurementRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Readings logged against batches, and the chart series built from them.
 */
@Service
@Transactional(readOnly = true)
class MeasurementService(
    private val measurementRepository: MeasurementRepository
) {

    fun findAll(batch: Batch): List<MeasurementResponse> =
        findAllEntities(batch).map { it.toResponse() }

    /**
     * Raw readings for a batch in time order, for callers that need to derive several things from the
     * same list without querying twice.
     */
    fun findAllEntities(batch: Batch): List<Measurement> =
        measurementRepository.findAllByBatchIdOrderByRecordedAtAsc(requireNotNull(batch.id))

    fun findByMetric(batch: Batch, metric: MetricType): List<MeasurementResponse> =
        measurementRepository
            .findAllByBatchIdAndMetricOrderByRecordedAtAsc(requireNotNull(batch.id), metric)
            .map { it.toResponse() }

    /**
     * Newest reading per metric for each of [batchIds], grouped by batch.
     */
    fun findLatestPerMetric(batchIds: Collection<UUID>): Map<UUID, List<Measurement>> {
        if (batchIds.isEmpty()) {
            return emptyMap()
        }

        return measurementRepository
            .findLatestPerMetric(batchIds)
            .groupBy { requireNotNull(it.batch.id) }
    }

    fun countByBatch(batchId: UUID): Long =
        measurementRepository.countByBatchId(batchId)

    @Transactional
    fun add(batch: Batch, request: MeasurementAddRequest): MeasurementResponse {
        val measurement = Measurement(
            batch = batch,
            metric = request.metric,
            value = request.value,
            unit = request.unit?.takeIf { it.isNotBlank() } ?: request.metric.defaultUnit,
            recordedAt = request.recordedAt ?: OffsetDateTime.now(),
            note = request.note
        )

        return measurementRepository.save(measurement).toResponse()
    }

    @Transactional
    fun delete(id: UUID) {
        val measurement = measurementRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("Measurement", id) }

        measurementRepository.delete(measurement)
    }

    /**
     * Builds one chart line per metric that has at least one reading, annotated with the healthy
     * window and the sour limit for the batch's style so the frontend can draw the guide bands.
     */
    fun buildSeries(batch: Batch, measurements: List<Measurement>): List<MetricSeriesResponse> =
        measurements
            .groupBy { it.metric }
            .toSortedMap()
            .map { (metric, readings) -> buildSeries(batch, metric, readings) }

    /**
     * Builds the series for a single metric.
     */
    fun buildSeries(batch: Batch, metric: MetricType, readings: List<Measurement>): MetricSeriesResponse {
        val fermentType = batch.fermentType

        return MetricSeriesResponse(
            metric = metric,
            unit = readings.firstOrNull()?.unit ?: metric.defaultUnit,
            targetMin = when (metric) {
                MetricType.PH -> fermentType.targetPhMin
                MetricType.TEMPERATURE -> fermentType.targetTempMinC
                else -> null
            },
            targetMax = when (metric) {
                MetricType.PH -> fermentType.targetPhMax
                MetricType.TEMPERATURE -> fermentType.targetTempMaxC
                else -> null
            },
            dangerThreshold = if (metric == MetricType.PH) fermentType.overFermentationPh else null,
            points = readings
                .sortedBy { it.recordedAt }
                .map {
                    MetricSeriesResponse.MetricPointResponse(
                        recordedAt = it.recordedAt,
                        dayOffset = ChronoUnit.DAYS
                            .between(batch.startedOn, it.recordedAt.toLocalDate())
                            .coerceAtLeast(0),
                        value = it.value
                    )
                }
        )
    }
}
