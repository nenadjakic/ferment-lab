package com.github.nenadjakic.fermentlab.restapp.controller

import com.github.nenadjakic.fermentlab.common.dto.alert.AlertResponse
import com.github.nenadjakic.fermentlab.service.AlertService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/alert")
@Validated
@Tag(
    name = "Alert",
    description = "The notification centre: milestone reminders, over-fermentation warnings and feed reminders."
)
class AlertController(
    private val alertService: AlertService
) {

    @GetMapping
    @Operation(
        operationId = "getAlerts",
        summary = "List alerts, most urgent first. Open alerts only unless includeAcknowledged is set."
    )
    fun findAll(
        @RequestParam(required = false, defaultValue = "false") includeAcknowledged: Boolean
    ): ResponseEntity<List<AlertResponse>> =
        ResponseEntity.ok(alertService.findAll(openOnly = !includeAcknowledged))

    @PostMapping("/{id}/acknowledge")
    @Operation(operationId = "acknowledgeAlert", summary = "Dismiss a single alert.")
    fun acknowledge(@PathVariable id: UUID): ResponseEntity<AlertResponse> =
        ResponseEntity.ok(alertService.acknowledge(id))

    @PostMapping("/acknowledge-all")
    @Operation(operationId = "acknowledgeAllAlerts", summary = "Dismiss every open alert.")
    fun acknowledgeAll(): ResponseEntity<Map<String, Int>> =
        ResponseEntity.ok(mapOf("acknowledged" to alertService.acknowledgeAll()))

    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteAlert", summary = "Delete an alert outright.")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        alertService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
