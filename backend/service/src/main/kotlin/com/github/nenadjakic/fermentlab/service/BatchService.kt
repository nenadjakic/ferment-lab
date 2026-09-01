package com.github.nenadjakic.fermentlab.service

import com.github.nenadjakic.fermentlab.common.dto.batch.BatchAddRequest
import com.github.nenadjakic.fermentlab.common.dto.batch.BatchCompleteRequest
import com.github.nenadjakic.fermentlab.common.dto.batch.BatchDetailResponse
import com.github.nenadjakic.fermentlab.common.dto.batch.BatchEditRequest
import com.github.nenadjakic.fermentlab.common.dto.batch.BatchEventAddRequest
import com.github.nenadjakic.fermentlab.common.dto.batch.BatchEventResponse
import com.github.nenadjakic.fermentlab.common.dto.batch.BatchResponse
import com.github.nenadjakic.fermentlab.common.dto.batch.BatchStageAddRequest
import com.github.nenadjakic.fermentlab.common.dto.batch.BatchStageResponse
import com.github.nenadjakic.fermentlab.common.dto.batch.MeasurementAddRequest
import com.github.nenadjakic.fermentlab.common.dto.batch.MeasurementResponse
import com.github.nenadjakic.fermentlab.common.exception.BusinessRuleException
import com.github.nenadjakic.fermentlab.common.exception.ResourceNotFoundException
import com.github.nenadjakic.fermentlab.common.extension.toResponse
import com.github.nenadjakic.fermentlab.data.entity.batch.Batch
import com.github.nenadjakic.fermentlab.data.entity.batch.BatchEvent
import com.github.nenadjakic.fermentlab.data.entity.batch.BatchStage
import com.github.nenadjakic.fermentlab.data.enum.BatchEventType
import com.github.nenadjakic.fermentlab.data.enum.BatchStatus
import com.github.nenadjakic.fermentlab.data.enum.StagePhase
import com.github.nenadjakic.fermentlab.data.repository.AlertRepository
import com.github.nenadjakic.fermentlab.data.repository.BatchEventRepository
import com.github.nenadjakic.fermentlab.data.repository.BatchRepository
import com.github.nenadjakic.fermentlab.data.repository.BatchStageRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

/**
 * The batch aggregate: creation from a recipe, the stage timeline, the journal and completion.
 */
@Service
@Transactional(readOnly = true)
class BatchService(
    private val batchRepository: BatchRepository,
    private val batchStageRepository: BatchStageRepository,
    private val batchEventRepository: BatchEventRepository,
    private val alertRepository: AlertRepository,
    private val measurementService: MeasurementService,
    private val fermentTypeService: FermentTypeService,
    private val recipeService: RecipeService,
    private val cultureService: CultureService
) {

    fun search(
        search: String?,
        fermentTypeId: UUID?,
        status: BatchStatus?,
        pageable: Pageable
    ): Page<BatchResponse> {
        val page = batchRepository.search(search?.takeIf { it.isNotBlank() }, fermentTypeId, status, pageable)
        return page.map(::decorate)
    }

    /**
     * Every batch that has not been closed, newest first. Backs the dashboard and the compare picker.
     */
    fun findOpen(): List<BatchResponse> =
        decorateAll(
            batchRepository.findAllByStatusInOrderByStartedOnDesc(
                listOf(BatchStatus.PLANNED, BatchStatus.ACTIVE, BatchStatus.PAUSED)
            )
        )

    fun findById(id: UUID): BatchDetailResponse {
        val batch = getEntity(id)
        val measurements = measurementService.findAllEntities(batch)

        return BatchDetailResponse(
            batch = batch.toResponse(
                latestMeasurements = measurements
                    .groupBy { it.metric }
                    .values
                    .mapNotNull { readings -> readings.maxByOrNull { it.recordedAt } },
                openAlertCount = alertRepository
                    .findAllByBatchIdOrderByDueAtDesc(id)
                    .count { it.acknowledgedAt == null }
            ),
            notes = batch.notes,
            stages = batchStageRepository.findAllByBatchIdOrderByPositionAsc(id).map { it.toResponse() },
            events = batchEventRepository.findAllByBatchIdOrderByOccurredAtDesc(id).map { it.toResponse() },
            series = measurementService.buildSeries(batch, measurements),
            alerts = alertRepository.findAllByBatchIdOrderByDueAtDesc(id).map { it.toResponse() },
            measurementCount = measurements.size.toLong()
        )
    }

    /**
     * Starts a batch.
     *
     * When the request names a recipe, the recipe's steps are copied onto the batch as its stage
     * timeline and the planned finish comes from the recipe duration. Without a recipe the ferment
     * type's default duration is used. An explicit end date in the request always wins.
     */
    @Transactional
    fun add(request: BatchAddRequest): BatchDetailResponse {
        val fermentType = fermentTypeService.getEntity(request.fermentTypeId)
        val recipe = request.recipeId?.let { recipeService.getEntity(it) }
        val culture = request.cultureId?.let { cultureService.getEntity(it) }

        if (recipe != null && recipe.fermentType.id != fermentType.id) {
            throw BusinessRuleException(
                "Recipe '${recipe.name}' is for ${recipe.fermentType.name}, not ${fermentType.name}."
            )
        }

        val startedOn = request.startedOn ?: LocalDate.now()
        val plannedDays = recipe?.totalDurationDays ?: fermentType.defaultDurationDays
        val expectedEndOn = request.expectedEndOn ?: startedOn.plusDays(plannedDays.toLong())

        if (expectedEndOn.isBefore(startedOn)) {
            throw BusinessRuleException("The planned finish cannot be before the start date.")
        }

        val batch = batchRepository.save(
            Batch(
                code = nextCode(fermentType.code),
                name = request.name,
                fermentType = fermentType,
                recipe = recipe,
                culture = culture,
                status = if (startedOn.isAfter(LocalDate.now())) BatchStatus.PLANNED else BatchStatus.ACTIVE,
                startedOn = startedOn,
                expectedEndOn = expectedEndOn,
                container = request.container,
                batchSize = request.batchSize,
                batchSizeUnit = request.batchSizeUnit ?: "L",
                location = request.location,
                notes = request.notes
            )
        )

        if (recipe != null) {
            copyStagesFromRecipe(batch, requireNotNull(recipe.id))
        }

        return findById(requireNotNull(batch.id))
    }

    @Transactional
    fun update(id: UUID, request: BatchEditRequest): BatchDetailResponse {
        val batch = getEntity(id)

        if (request.expectedEndOn.isBefore(batch.startedOn)) {
            throw BusinessRuleException("The planned finish cannot be before the start date.")
        }

        batch.apply {
            name = request.name
            expectedEndOn = request.expectedEndOn
            culture = request.cultureId?.let { cultureService.getEntity(it) }
            container = request.container
            batchSize = request.batchSize
            batchSizeUnit = request.batchSizeUnit
            location = request.location
            rating = request.rating
            notes = request.notes
        }

        batchRepository.save(batch)
        return findById(id)
    }

    /**
     * Closes a batch, finishing any stage still running and recording the outcome on the journal.
     */
    @Transactional
    fun complete(id: UUID, request: BatchCompleteRequest): BatchDetailResponse {
        val batch = getEntity(id)

        if (!batch.status.isOpen) {
            throw BusinessRuleException("Batch ${batch.code} is already closed.")
        }

        val endedOn = request.endedOn ?: LocalDate.now()
        if (endedOn.isBefore(batch.startedOn)) {
            throw BusinessRuleException("The end date cannot be before the start date.")
        }

        val closedAt = endedOn.atTime(12, 0).atOffset(OffsetDateTime.now().offset)

        batchStageRepository
            .findAllByBatchIdOrderByPositionAsc(id)
            .filter { it.completedAt == null }
            .forEach { stage ->
                stage.startedAt = stage.startedAt ?: closedAt
                stage.completedAt = closedAt
                batchStageRepository.save(stage)
            }

        batch.apply {
            status = BatchStatus.COMPLETED
            outcome = request.outcome
            rating = request.rating ?: rating
            this.endedOn = endedOn
            notes = request.notes ?: notes
        }
        batchRepository.save(batch)

        batchEventRepository.save(
            BatchEvent(
                batch = batch,
                eventType = BatchEventType.HARVEST,
                occurredAt = closedAt,
                title = "Batch completed",
                description = "Outcome ${request.outcome}${request.rating?.let { ", rated $it/5" } ?: ""}."
            )
        )

        return findById(id)
    }

    /**
     * Moves an open batch to a new lifecycle state. Completion goes through [complete] instead,
     * because it needs an outcome.
     */
    @Transactional
    fun changeStatus(id: UUID, status: BatchStatus): BatchDetailResponse {
        val batch = getEntity(id)

        if (status == BatchStatus.COMPLETED) {
            throw BusinessRuleException("Use the complete endpoint to finish a batch, so an outcome is recorded.")
        }

        batch.status = status
        if (status == BatchStatus.DISCARDED && batch.endedOn == null) {
            batch.endedOn = LocalDate.now()
        }
        batchRepository.save(batch)

        return findById(id)
    }

    @Transactional
    fun delete(id: UUID) {
        batchRepository.delete(getEntity(id))
    }

    // ------------------------------------------------------------------------------------------
    // Timeline
    // ------------------------------------------------------------------------------------------

    fun findStages(batchId: UUID): List<BatchStageResponse> {
        getEntity(batchId)
        return batchStageRepository.findAllByBatchIdOrderByPositionAsc(batchId).map { it.toResponse() }
    }

    @Transactional
    fun addStage(batchId: UUID, request: BatchStageAddRequest): BatchStageResponse {
        val batch = getEntity(batchId)
        val nextPosition = (batchStageRepository
            .findAllByBatchIdOrderByPositionAsc(batchId)
            .maxOfOrNull { it.position } ?: 0) + 1

        val stage = batchStageRepository.save(
            BatchStage(
                batch = batch,
                position = nextPosition,
                name = request.name,
                phase = request.phase,
                instruction = request.instruction,
                plannedDurationDays = request.plannedDurationDays
            )
        )

        return stage.toResponse()
    }

    /**
     * Starts a stage and records it on the journal.
     */
    @Transactional
    fun startStage(batchId: UUID, stageId: UUID): BatchStageResponse {
        val stage = getStage(batchId, stageId)

        if (stage.startedAt != null) {
            throw BusinessRuleException("Stage '${stage.name}' has already been started.")
        }

        stage.startedAt = OffsetDateTime.now()
        batchStageRepository.save(stage)

        batchEventRepository.save(
            BatchEvent(
                batch = stage.batch,
                eventType = BatchEventType.STAGE_STARTED,
                title = "Started: ${stage.name}",
                description = stage.instruction
            )
        )

        return stage.toResponse()
    }

    /**
     * Completes a stage, and starts the next pending one so the timeline keeps moving.
     */
    @Transactional
    fun completeStage(batchId: UUID, stageId: UUID): BatchStageResponse {
        val stage = getStage(batchId, stageId)

        if (stage.completedAt != null) {
            throw BusinessRuleException("Stage '${stage.name}' is already complete.")
        }

        val now = OffsetDateTime.now()
        stage.startedAt = stage.startedAt ?: now
        stage.completedAt = now
        batchStageRepository.save(stage)

        batchEventRepository.save(
            BatchEvent(
                batch = stage.batch,
                eventType = BatchEventType.STAGE_COMPLETED,
                title = "Completed: ${stage.name}"
            )
        )

        batchStageRepository
            .findAllByBatchIdOrderByPositionAsc(batchId)
            .firstOrNull { it.position > stage.position && it.startedAt == null }
            ?.let {
                it.startedAt = now
                batchStageRepository.save(it)
            }

        return stage.toResponse()
    }

    // ------------------------------------------------------------------------------------------
    // Journal and readings
    // ------------------------------------------------------------------------------------------

    fun findEvents(batchId: UUID): List<BatchEventResponse> {
        getEntity(batchId)
        return batchEventRepository.findAllByBatchIdOrderByOccurredAtDesc(batchId).map { it.toResponse() }
    }

    @Transactional
    fun addEvent(batchId: UUID, request: BatchEventAddRequest): BatchEventResponse {
        val batch = getEntity(batchId)

        return batchEventRepository.save(
            BatchEvent(
                batch = batch,
                eventType = request.eventType,
                occurredAt = request.occurredAt ?: OffsetDateTime.now(),
                title = request.title,
                description = request.description
            )
        ).toResponse()
    }

    fun findMeasurements(batchId: UUID): List<MeasurementResponse> =
        measurementService.findAll(getEntity(batchId))

    @Transactional
    fun addMeasurement(batchId: UUID, request: MeasurementAddRequest): MeasurementResponse =
        measurementService.add(getEntity(batchId), request)

    // ------------------------------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------------------------------

    fun getEntity(id: UUID): Batch =
        batchRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("Batch", id) }

    /**
     * Attaches the derived fields - latest readings, open alert count and health - to a batch card.
     */
    fun decorate(batch: Batch): BatchResponse {
        val id = requireNotNull(batch.id)

        return batch.toResponse(
            latestMeasurements = measurementService.findLatestPerMetric(listOf(id))[id].orEmpty(),
            openAlertCount = alertRepository
                .findAllByBatchIdOrderByDueAtDesc(id)
                .count { it.acknowledgedAt == null }
        )
    }

    /**
     * Bulk variant of [decorate] that resolves the derived fields for a whole list in one query
     * each, so list endpoints do not fan out into per-row lookups.
     */
    fun decorateAll(batches: List<Batch>): List<BatchResponse> {
        if (batches.isEmpty()) {
            return emptyList()
        }

        val ids = batches.mapNotNull { it.id }
        val latestByBatch = measurementService.findLatestPerMetric(ids)
        val openAlertsByBatch = alertRepository
            .findAllByAcknowledgedAtIsNullOrderByDueAtAsc()
            .mapNotNull { it.batch?.id }
            .groupingBy { it }
            .eachCount()

        return batches.map { batch ->
            val id = requireNotNull(batch.id)
            batch.toResponse(
                latestMeasurements = latestByBatch[id].orEmpty(),
                openAlertCount = openAlertsByBatch[id] ?: 0
            )
        }
    }

    private fun getStage(batchId: UUID, stageId: UUID): BatchStage =
        batchStageRepository
            .findAllByBatchIdOrderByPositionAsc(batchId)
            .firstOrNull { it.id == stageId }
            ?: throw ResourceNotFoundException("Stage $stageId was not found on batch $batchId.")

    /**
     * Copies a recipe's method onto a batch as its stage timeline, and starts the first stage so the
     * batch is immediately live.
     */
    private fun copyStagesFromRecipe(batch: Batch, recipeId: UUID) {
        val stages = recipeService.findSteps(recipeId).map { step ->
            BatchStage(
                batch = batch,
                position = step.position,
                name = step.title,
                phase = step.phase,
                instruction = step.instruction,
                plannedDurationDays = step.durationDays
            )
        }

        if (stages.isEmpty()) {
            return
        }

        val now = OffsetDateTime.now()
        stages.filter { it.phase == StagePhase.PREP }.forEach {
            it.startedAt = now
            it.completedAt = now
        }
        stages.firstOrNull { it.phase != StagePhase.PREP }?.startedAt = now

        batchStageRepository.saveAll(stages)
    }

    /**
     * Generates the next human friendly batch code for a style, e.g. `KOM-0013`.
     */
    private fun nextCode(fermentTypeCode: String): String {
        val prefix = fermentTypeCode.filter { it.isLetter() }.take(3).uppercase().padEnd(3, 'X')

        val highest = batchRepository
            .findAllByCodeStartingWith("$prefix-")
            .mapNotNull { it.code.substringAfterLast('-').toIntOrNull() }
            .maxOrNull() ?: 0

        return "%s-%04d".format(prefix, highest + 1)
    }
}
