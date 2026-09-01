package com.github.nenadjakic.fermentlab.restapp.controller

import com.github.nenadjakic.fermentlab.common.dto.recipe.RecipeAddRequest
import com.github.nenadjakic.fermentlab.common.dto.recipe.RecipeDetailResponse
import com.github.nenadjakic.fermentlab.common.dto.recipe.RecipeResponse
import com.github.nenadjakic.fermentlab.service.RecipeService
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
@RequestMapping("/recipe")
@Validated
@Tag(name = "Recipe", description = "Curated recipes and guides that batches can be started from.")
class RecipeController(
    private val recipeService: RecipeService
) {

    @GetMapping
    @Operation(operationId = "getRecipes", summary = "List recipes, optionally filtered by style.")
    fun findAll(
        @RequestParam(required = false) fermentTypeId: UUID?,
        @RequestParam(required = false, defaultValue = "false") featuredOnly: Boolean
    ): ResponseEntity<List<RecipeResponse>> =
        ResponseEntity.ok(recipeService.findAll(fermentTypeId, featuredOnly))

    @GetMapping("/{id}")
    @Operation(
        operationId = "getRecipeById",
        summary = "Get a recipe with its ingredients and step by step method."
    )
    fun findById(@PathVariable id: UUID): ResponseEntity<RecipeDetailResponse> =
        ResponseEntity.ok(recipeService.findById(id))

    @PostMapping
    @Operation(
        operationId = "addRecipe",
        summary = "Create a recipe. The total duration is derived from the step durations."
    )
    fun add(@Valid @RequestBody request: RecipeAddRequest): ResponseEntity<RecipeDetailResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(recipeService.add(request))

    @PutMapping("/{id}")
    @Operation(
        operationId = "updateRecipe",
        summary = "Update a recipe, replacing its ingredient list and method. The total duration is " +
            "re-derived from the step durations."
    )
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RecipeAddRequest
    ): ResponseEntity<RecipeDetailResponse> =
        ResponseEntity.ok(recipeService.update(id, request))

    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteRecipe", summary = "Delete a recipe.")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        recipeService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
