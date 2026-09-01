package com.github.nenadjakic.fermentlab.service

import com.github.nenadjakic.fermentlab.common.dto.NameValueResponse
import com.github.nenadjakic.fermentlab.common.dto.analytics.BatchComparisonResponse
import com.github.nenadjakic.fermentlab.common.dto.analytics.DashboardSummaryResponse
import com.github.nenadjakic.fermentlab.common.dto.analytics.MetricSeriesResponse
import com.github.nenadjakic.fermentlab.common.dto.analytics.MilestoneResponse
import com.github.nenadjakic.fermentlab.common.enum.BatchHealth
import com.github.nenadjakic.fermentlab.common.exception.BusinessRuleException
import com.github.nenadjakic.fermentlab.common.extension.toResponse
import com.github.nenadjakic.fermentlab.data.entity.batch.Batch
import com.github.nenadjakic.fermentlab.data.enum.BatchStatus
import com.github.nenadjakic.fermentlab.data.enum.MetricType
import com.github.nenadjakic.fermentlab.data.repository.BatchEventRepository
import com.github.nenadjakic.fermentlab.data.repository.BatchRepository
import com.github.nenadjakic.fermentlab.data.repository.BatchStageRepository
import com.github.nenadjakic.fermentlab.data.repository.CultureRepository
import com.github.nenadjakic.fermentlab.data.repository.MeasurementRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Read only analytics: the dashboard roll-up and the side by side batch comparison.
 */
@Service
@Transactional(readOnly = true)
class AnalyticsService(
    private val batchRepository: BatchRepository,
    private val batchStageRepository: BatchStageRepository,
    private val batchEventRepository: BatchEventRepository,
    private val measurementRepository: MeasurementRepository,
    private val cultureRepository: CultureRepository,
    private val batchService: BatchService,
    private val measurementService: MeasurementService,
    private val alertService: AlertService
) {

    /**
     * Assembles the whole dashboard in one pass so the frontend needs a single request.
     */
    fun dashboard(): DashboardSummaryResponse {
        val openBatches = batchRepository.findAllByStatusInOrderByStartedOnDesc(OPEN_STATUSES)
        val cards = batchService.decorateAll(openBatches)

        return DashboardSummaryResponse(
            activeBatchCount = batchRepository.countByStatus(BatchStatus.ACTIVE),
            plannedBatchCount = batchRepository.countByStatus(BatchStatus.PLANNED),
            completedBatchCount = batchRepository.countByStatus(BatchStatus.COMPLETED),
            activeCultureCount = cultureRepository.countByActiveTrue(),
            openAlertCount = alertService.countOpen(),
            averageRating = batchRepository.averageRating(),
            batchesPerFermentType = batchRepository.countGroupedByFermentType().map { row ->
                NameValueResponse(name = row[0] as String, value = (row[1] as Number).toLong())
            },
            activeBatches = cards,
            attentionBatches = cards.filter { it.health in ATTENTION_STATES },
            upcomingMilestones = milestones(openBatches),
            recentEvents = batchEventRepository.findTop12ByOrderByOccurredAtDesc().map { it.toResponse() },
            openAlerts = alertService.findAll(openOnly = true)
        )
    }

    /**
     * The next things that need doing across every open batch, soonest first.
     *
     * A milestone is either a running stage reaching its planned end, or - for batches without a
     * stage timeline - the batch itself reaching its planned finish.
     */
    fun milestones(openBatches: List<Batch> = batchRepository.findAllByStatusInOrderByStartedOnDesc(OPEN_STATUSES)): List<MilestoneResponse> {
        val now = OffsetDateTime.now()
        val batchIds = openBatches.mapNotNull { it.id }
        if (batchIds.isEmpty()) {
            return emptyList()
        }

        val stagesByBatch = batchStageRepository
            .findAllByBatchIdInOrderByPositionAsc(batchIds)
            .groupBy { requireNotNull(it.batch.id) }

        return openBatches.flatMap { batch ->
            val id = requireNotNull(batch.id)
            val running = stagesByBatch[id]
                .orEmpty()
                .filter { it.completedAt == null && it.startedAt != null && it.plannedDurationDays > 0 }

            val stageMilestones = running.map { stage ->
                val dueAt = requireNotNull(stage.startedAt).plusDays(stage.plannedDurationDays.toLong())
                MilestoneResponse(
                    batchId = id,
                    batchCode = batch.code,
                    batchName = batch.name,
                    stageId = stage.id,
                    title = stage.name,
                    dueAt = dueAt,
                    daysRemaining = ChronoUnit.DAYS.between(now, dueAt)
                )
            }

            stageMilestones.ifEmpty {
                val dueAt = batch.expectedEndOn.atTime(12, 0).atOffset(now.offset)
                listOf(
                    MilestoneResponse(
                        batchId = id,
                        batchCode = batch.code,
                        batchName = batch.name,
                        stageId = null,
                        title = "Planned finish",
                        dueAt = dueAt,
                        daysRemaining = ChronoUnit.DAYS.between(now, dueAt)
                    )
                )
            }
        }.sortedBy { it.dueAt }
    }

    /**
     * Overlays several batches on the same chart.
     *
     * Series are indexed by day offset rather than by date, which is what makes two batches started
     * weeks apart directly comparable.
     *
     * @param batchIds two to six batches.
     * @param metrics metrics to plot; defaults to pH and temperature.
     */
    fun compare(batchIds: List<UUID>, metrics: List<MetricType>): BatchComparisonResponse {
        if (batchIds.size < 2) {
            throw BusinessRuleException("Pick at least two batches to compare.")
        }
        if (batchIds.size > MAX_COMPARED_BATCHES) {
            throw BusinessRuleException("Comparing more than $MAX_COMPARED_BATCHES batches at once is not supported.")
        }

        val requestedMetrics = metrics.ifEmpty { DEFAULT_COMPARISON_METRICS }
        val batches = batchRepository.findAllById(batchIds)

        val missing = batchIds - batches.mapNotNull { it.id }.toSet()
        if (missing.isNotEmpty()) {
            throw BusinessRuleException("Unknown batch id(s): ${missing.joinToString()}")
        }

        val readingsByBatch = measurementRepository
            .findAllByBatchIdInOrderByRecordedAtAsc(batchIds)
            .groupBy { requireNotNull(it.batch.id) }

        // Preserve the order the caller asked for, so chart colours stay stable across requests.
        val byId = batches.associateBy { requireNotNull(it.id) }

        return BatchComparisonResponse(
            metrics = requestedMetrics,
            batches = batchIds.mapNotNull { byId[it] }.map { batch ->
                val id = requireNotNull(batch.id)
                val readings = readingsByBatch[id].orEmpty()

                BatchComparisonResponse.ComparedBatchResponse(
                    batchId = id,
                    code = batch.code,
                    name = batch.name,
                    fermentTypeName = batch.fermentType.name,
                    accentColor = batch.fermentType.accentColor,
                    startedOn = batch.startedOn,
                    dayCount = batch.dayCount,
                    rating = batch.rating,
                    outcome = batch.outcome,
                    series = requestedMetrics.map { metric ->
                        measurementService.buildSeries(batch, metric, readings.filter { it.metric == metric })
                    }
                )
            }
        )
    }

    /**
     * Every chart line for a single batch.
     */
    fun series(batchId: UUID): List<MetricSeriesResponse> {
        val batch = batchService.getEntity(batchId)
        return measurementService.buildSeries(batch, measurementService.findAllEntities(batch))
    }

    private companion object {

        val OPEN_STATUSES = listOf(BatchStatus.PLANNED, BatchStatus.ACTIVE, BatchStatus.PAUSED)

        val ATTENTION_STATES = setOf(
            BatchHealth.OVER_FERMENTED,
            BatchHealth.ATTENTION,
            BatchHealth.READY
        )

        val DEFAULT_COMPARISON_METRICS = listOf(MetricType.PH, MetricType.TEMPERATURE)

        const val MAX_COMPARED_BATCHES = 6
    }
}
