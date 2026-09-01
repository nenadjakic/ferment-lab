package com.github.nenadjakic.fermentlab.restapp.controller

import com.github.nenadjakic.fermentlab.common.dto.culture.CultureAddRequest
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureFeedingRequest
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureFeedingResponse
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureResponse
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureSplitRequest
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureTreeNodeResponse
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureUpdateRequest
import com.github.nenadjakic.fermentlab.service.CultureService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/culture")
@Validated
@Tag(
    name = "Culture",
    description = "The culture library: SCOBYs, starters and grains, their care log and their family trees."
)
class CultureController(
    private val cultureService: CultureService
) {

    @GetMapping
    @Operation(operationId = "getCultures", summary = "List cultures.")
    fun findAll(
        @RequestParam(required = false, defaultValue = "false") activeOnly: Boolean
    ): ResponseEntity<List<CultureResponse>> =
        ResponseEntity.ok(cultureService.findAll(activeOnly))

    @GetMapping("/tree")
    @Operation(
        operationId = "getCultureTree",
        summary = "The whole library as family trees, one per directly acquired culture."
    )
    fun findForest(): ResponseEntity<List<CultureTreeNodeResponse>> =
        ResponseEntity.ok(cultureService.findForest())

    @GetMapping("/{id}")
    @Operation(operationId = "getCultureById", summary = "Get a single culture.")
    fun findById(@PathVariable id: UUID): ResponseEntity<CultureResponse> =
        ResponseEntity.ok(cultureService.findById(id))

    @PostMapping
    @Operation(operationId = "addCulture", summary = "Add a culture to the library.")
    fun add(@Valid @RequestBody request: CultureAddRequest): ResponseEntity<CultureResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(cultureService.add(request))

    @PutMapping("/{id}")
    @Operation(operationId = "updateCulture", summary = "Edit a culture.")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CultureUpdateRequest
    ): ResponseEntity<CultureResponse> =
        ResponseEntity.ok(cultureService.update(id, request))

    @PostMapping("/{id}/split")
    @Operation(
        operationId = "splitCulture",
        summary = "Split a culture, creating a child one generation down the family tree."
    )
    fun split(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CultureSplitRequest
    ): ResponseEntity<CultureResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(cultureService.split(id, request))

    @GetMapping("/{id}/feeding")
    @Operation(operationId = "getCultureFeedings", summary = "The care log of a culture.")
    fun findFeedings(@PathVariable id: UUID): ResponseEntity<List<CultureFeedingResponse>> =
        ResponseEntity.ok(cultureService.findFeedings(id))

    @PostMapping("/{id}/feeding")
    @Operation(
        operationId = "feedCulture",
        summary = "Log a feeding, which also moves the culture's feed schedule forward."
    )
    fun feed(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CultureFeedingRequest
    ): ResponseEntity<CultureFeedingResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(cultureService.feed(id, request))

    @DeleteMapping("/{id}")
    @Operation(
        operationId = "deleteCulture",
        summary = "Delete a culture. Rejected while it still has descendants."
    )
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        cultureService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
