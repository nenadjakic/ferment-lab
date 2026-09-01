package com.github.nenadjakic.fermentlab.restapp.controller

import com.github.nenadjakic.fermentlab.common.dto.reference.FermentTypeResponse
import com.github.nenadjakic.fermentlab.service.FermentTypeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/ferment-type")
@Validated
@Tag(name = "Ferment type", description = "Reference data for the supported fermentation styles.")
class FermentTypeController(
    private val fermentTypeService: FermentTypeService
) {

    @GetMapping
    @Operation(
        operationId = "getFermentTypes",
        summary = "List every ferment style with its healthy target windows."
    )
    fun findAll(): ResponseEntity<List<FermentTypeResponse>> =
        ResponseEntity.ok(fermentTypeService.findAll())

    @GetMapping("/{id}")
    @Operation(operationId = "getFermentTypeById", summary = "Get a single ferment style.")
    fun findById(@PathVariable id: UUID): ResponseEntity<FermentTypeResponse> =
        ResponseEntity.ok(fermentTypeService.findById(id))
}
