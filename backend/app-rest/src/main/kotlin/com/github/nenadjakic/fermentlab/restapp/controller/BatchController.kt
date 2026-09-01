package com.github.nenadjakic.fermentlab.restapp.controller

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
import com.github.nenadjakic.fermentlab.data.enum.BatchStatus
import com.github.nenadjakic.fermentlab.service.BatchService
import com.github.nenadjakic.fermentlab.service.MeasurementService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/batch")
@Validated
@Tag(name = "Batch", description = "Fermentation batches, their timeline, journal and readings.")
class BatchController(
    private val batchService: BatchService,
    private val measurementService: MeasurementService
) {

    @GetMapping
    @Operation(
        operationId = "getBatches",
        summary = "Paged batch search by free text, style and status."
    )
    fun search(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) fermentTypeId: UUID?,
        @RequestParam(required = false) status: BatchStatus?,
        @PageableDefault(size = 20, sort = ["startedOn"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<BatchResponse>> =
        ResponseEntity.ok(batchService.search(search, fermentTypeId, status, pageable))

    @GetMapping("/open")
    @Operation(
        operationId = "getOpenBatches",
        summary = "Every batch that has not been closed, newest first."
    )
    fun findOpen(): ResponseEntity<List<BatchResponse>> =
        ResponseEntity.ok(batchService.findOpen())

    @GetMapping("/{id}")
    @Operation(
        operationId = "getBatchById",
        summary = "Full batch detail: card, timeline, journal, chart series and alerts."
    )
    fun findById(@PathVariable id: UUID): ResponseEntity<BatchDetailResponse> =
        ResponseEntity.ok(batchService.findById(id))

    @PostMapping
    @Operation(
        operationId = "addBatch",
        summary = "Start a batch. Naming a recipe copies its method onto the batch as the timeline."
    )
    fun add(@Valid @RequestBody request: BatchAddRequest): ResponseEntity<BatchDetailResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(batchService.add(request))

    @PutMapping("/{id}")
    @Operation(operationId = "updateBatch", summary = "Edit a batch.")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: BatchEditRequest
    ): ResponseEntity<BatchDetailResponse> =
        ResponseEntity.ok(batchService.update(id, request))

    @PatchMapping("/{id}/status")
    @Operation(
        operationId = "changeBatchStatus",
        summary = "Pause, resume, discard or archive a batch."
    )
    fun changeStatus(
        @PathVariable id: UUID,
        @RequestParam status: BatchStatus
    ): ResponseEntity<BatchDetailResponse> =
        ResponseEntity.ok(batchService.changeStatus(id, status))

    @PostMapping("/{id}/complete")
    @Operation(
        operationId = "completeBatch",
        summary = "Close a batch with an outcome, finishing any stage still running."
    )
    fun complete(
        @PathVariable id: UUID,
        @Valid @RequestBody request: BatchCompleteRequest
    ): ResponseEntity<BatchDetailResponse> =
        ResponseEntity.ok(batchService.complete(id, request))

    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteBatch", summary = "Delete a batch and everything attached to it.")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        batchService.delete(id)
        return ResponseEntity.noContent().build()
    }

    // ------------------------------------------------------------------------------------------
    // Timeline
    // ------------------------------------------------------------------------------------------

    @GetMapping("/{id}/stage")
    @Operation(operationId = "getBatchStages", summary = "The stage timeline of a batch.")
    fun findStages(@PathVariable id: UUID): ResponseEntity<List<BatchStageResponse>> =
        ResponseEntity.ok(batchService.findStages(id))

    @PostMapping("/{id}/stage")
    @Operation(operationId = "addBatchStage", summary = "Append a custom stage to the timeline.")
    fun addStage(
        @PathVariable id: UUID,
        @Valid @RequestBody request: BatchStageAddRequest
    ): ResponseEntity<BatchStageResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(batchService.addStage(id, request))

    @PostMapping("/{id}/stage/{stageId}/start")
    @Operation(operationId = "startBatchStage", summary = "Start a stage.")
    fun startStage(
        @PathVariable id: UUID,
        @PathVariable stageId: UUID
    ): ResponseEntity<BatchStageResponse> =
        ResponseEntity.ok(batchService.startStage(id, stageId))

    @PostMapping("/{id}/stage/{stageId}/complete")
    @Operation(
        operationId = "completeBatchStage",
        summary = "Complete a stage, which starts the next pending one."
    )
    fun completeStage(
        @PathVariable id: UUID,
        @PathVariable stageId: UUID
    ): ResponseEntity<BatchStageResponse> =
        ResponseEntity.ok(batchService.completeStage(id, stageId))

    // ------------------------------------------------------------------------------------------
    // Journal
    // ------------------------------------------------------------------------------------------

    @GetMapping("/{id}/event")
    @Operation(operationId = "getBatchEvents", summary = "The journal of a batch, newest first.")
    fun findEvents(@PathVariable id: UUID): ResponseEntity<List<BatchEventResponse>> =
        ResponseEntity.ok(batchService.findEvents(id))

    @PostMapping("/{id}/event")
    @Operation(operationId = "addBatchEvent", summary = "Add a journal entry.")
    fun addEvent(
        @PathVariable id: UUID,
        @Valid @RequestBody request: BatchEventAddRequest
    ): ResponseEntity<BatchEventResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(batchService.addEvent(id, request))

    // ------------------------------------------------------------------------------------------
    // Readings
    // ------------------------------------------------------------------------------------------

    @GetMapping("/{id}/measurement")
    @Operation(operationId = "getBatchMeasurements", summary = "Every reading on a batch, in time order.")
    fun findMeasurements(@PathVariable id: UUID): ResponseEntity<List<MeasurementResponse>> =
        ResponseEntity.ok(batchService.findMeasurements(id))

    @PostMapping("/{id}/measurement")
    @Operation(
        operationId = "addBatchMeasurement",
        summary = "Log a reading. The metric's default unit is used when none is supplied."
    )
    fun addMeasurement(
        @PathVariable id: UUID,
        @Valid @RequestBody request: MeasurementAddRequest
    ): ResponseEntity<MeasurementResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(batchService.addMeasurement(id, request))

    @DeleteMapping("/{id}/measurement/{measurementId}")
    @Operation(operationId = "deleteBatchMeasurement", summary = "Delete a reading.")
    fun deleteMeasurement(
        @PathVariable id: UUID,
        @PathVariable measurementId: UUID
    ): ResponseEntity<Void> {
        measurementService.delete(measurementId)
        return ResponseEntity.noContent().build()
    }
}
