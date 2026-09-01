package com.github.nenadjakic.fermentlab.restapp.controller

import com.github.nenadjakic.fermentlab.common.dto.analytics.BatchComparisonResponse
import com.github.nenadjakic.fermentlab.common.dto.analytics.DashboardSummaryResponse
import com.github.nenadjakic.fermentlab.common.dto.analytics.MetricSeriesResponse
import com.github.nenadjakic.fermentlab.common.dto.analytics.MilestoneResponse
import com.github.nenadjakic.fermentlab.data.enum.MetricType
import com.github.nenadjakic.fermentlab.service.AnalyticsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/analytics")
@Validated
@Tag(name = "Analytics", description = "Dashboard roll-up, chart series and side by side batch comparison.")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {

    @GetMapping("/dashboard")
    @Operation(
        operationId = "getDashboard",
        summary = "Everything the dashboard renders: counts, active batches, attention list, milestones and alerts."
    )
    fun dashboard(): ResponseEntity<DashboardSummaryResponse> =
        ResponseEntity.ok(analyticsService.dashboard())

    @GetMapping("/milestone")
    @Operation(
        operationId = "getMilestones",
        summary = "Upcoming milestones across every open batch, soonest first."
    )
    fun milestones(): ResponseEntity<List<MilestoneResponse>> =
        ResponseEntity.ok(analyticsService.milestones())

    @GetMapping("/batch/{id}/series")
    @Operation(
        operationId = "getBatchSeries",
        summary = "Every chart line for one batch, annotated with the healthy window for its style."
    )
    fun series(@PathVariable id: UUID): ResponseEntity<List<MetricSeriesResponse>> =
        ResponseEntity.ok(analyticsService.series(id))

    @GetMapping("/comparison")
    @Operation(
        operationId = "compareBatches",
        summary = "Overlay two to six batches, indexed by day offset so different start dates line up."
    )
    fun compare(
        @RequestParam batchId: List<UUID>,
        @RequestParam(required = false) metric: List<MetricType>?
    ): ResponseEntity<BatchComparisonResponse> =
        ResponseEntity.ok(analyticsService.compare(batchId, metric.orEmpty()))
}
