package com.github.nenadjakic.fermentlab.service

import com.github.nenadjakic.fermentlab.common.dto.culture.CultureAddRequest
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureFeedingRequest
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureFeedingResponse
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureResponse
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureSplitRequest
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureTreeNodeResponse
import com.github.nenadjakic.fermentlab.common.dto.culture.CultureUpdateRequest
import com.github.nenadjakic.fermentlab.common.exception.BusinessRuleException
import com.github.nenadjakic.fermentlab.common.exception.ResourceNotFoundException
import com.github.nenadjakic.fermentlab.common.extension.toForest
import com.github.nenadjakic.fermentlab.common.extension.toResponse
import com.github.nenadjakic.fermentlab.data.entity.culture.Culture
import com.github.nenadjakic.fermentlab.data.entity.culture.CultureFeeding
import com.github.nenadjakic.fermentlab.data.enum.CultureHealth
import com.github.nenadjakic.fermentlab.data.repository.CultureFeedingRepository
import com.github.nenadjakic.fermentlab.data.repository.CultureRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

/**
 * The culture library: cultures, their care log and their family tree.
 */
@Service
@Transactional(readOnly = true)
class CultureService(
    private val cultureRepository: CultureRepository,
    private val cultureFeedingRepository: CultureFeedingRepository,
    private val fermentTypeService: FermentTypeService
) {

    fun findAll(activeOnly: Boolean = false): List<CultureResponse> {
        val cultures = if (activeOnly) {
            cultureRepository.findAllByActiveTrueOrderByNameAsc()
        } else {
            cultureRepository.findAllByOrderByNameAsc()
        }

        val childCounts = cultures
            .mapNotNull { it.parent?.id }
            .groupingBy { it }
            .eachCount()

        return cultures.map { it.toResponse(childCounts[it.id] ?: 0) }
    }

    fun findById(id: UUID): CultureResponse {
        val culture = getEntity(id)
        return culture.toResponse(cultureRepository.findAllByParentIdOrderByNameAsc(id).size)
    }

    /**
     * The whole library folded into family trees, one per directly acquired culture.
     */
    fun findForest(): List<CultureTreeNodeResponse> =
        cultureRepository.findAllByOrderByNameAsc().toForest()

    fun findFeedings(cultureId: UUID): List<CultureFeedingResponse> {
        getEntity(cultureId)
        return cultureFeedingRepository.findAllByCultureIdOrderByFedAtDesc(cultureId).map { it.toResponse() }
    }

    @Transactional
    fun add(request: CultureAddRequest): CultureResponse {
        val parent = request.parentCultureId?.let { getEntity(it) }

        val culture = Culture(
            name = request.name,
            fermentType = fermentTypeService.getEntity(request.fermentTypeId),
            kind = request.kind,
            parent = parent,
            generation = (parent?.generation ?: 0) + 1,
            acquiredOn = request.acquiredOn ?: LocalDate.now(),
            health = request.health,
            feedIntervalDays = request.feedIntervalDays,
            flavorProfile = request.flavorProfile,
            notes = request.notes
        )

        return cultureRepository.save(culture).toResponse()
    }

    @Transactional
    fun update(id: UUID, request: CultureUpdateRequest): CultureResponse {
        val culture = getEntity(id).apply {
            name = request.name
            health = request.health
            feedIntervalDays = request.feedIntervalDays
            flavorProfile = request.flavorProfile
            notes = request.notes
            active = request.active
        }

        return cultureRepository.save(culture)
            .toResponse(cultureRepository.findAllByParentIdOrderByNameAsc(id).size)
    }

    /**
     * Splits a culture, creating a child one generation down that inherits the parent's style, kind
     * and feeding rhythm.
     */
    @Transactional
    fun split(id: UUID, request: CultureSplitRequest): CultureResponse {
        val parent = getEntity(id)
        if (!parent.active) {
            throw BusinessRuleException("Culture '${parent.name}' is retired and cannot be split.")
        }
        if (parent.health == CultureHealth.LOST) {
            throw BusinessRuleException("Culture '${parent.name}' is marked as lost and cannot be split.")
        }

        val child = Culture(
            name = request.name,
            fermentType = parent.fermentType,
            kind = parent.kind,
            parent = parent,
            generation = parent.generation + 1,
            acquiredOn = LocalDate.now(),
            health = CultureHealth.HEALTHY,
            feedIntervalDays = parent.feedIntervalDays,
            flavorProfile = parent.flavorProfile,
            notes = request.notes
        )

        return cultureRepository.save(child).toResponse()
    }

    /**
     * Logs a feeding and moves the culture's schedule forward.
     */
    @Transactional
    fun feed(id: UUID, request: CultureFeedingRequest): CultureFeedingResponse {
        val culture = getEntity(id)
        val fedAt = request.fedAt ?: OffsetDateTime.now()

        val feeding = cultureFeedingRepository.save(
            CultureFeeding(
                culture = culture,
                fedAt = fedAt,
                amount = request.amount,
                unit = request.unit,
                note = request.note
            )
        )

        val fedOn = fedAt.toLocalDate()
        if (culture.lastFedOn == null || fedOn.isAfter(culture.lastFedOn)) {
            culture.lastFedOn = fedOn
            cultureRepository.save(culture)
        }

        return feeding.toResponse()
    }

    /**
     * Retires a culture. Cultures are never hard deleted while they still have descendants, because
     * that would break the family tree.
     */
    @Transactional
    fun delete(id: UUID) {
        val culture = getEntity(id)
        val children = cultureRepository.findAllByParentIdOrderByNameAsc(id)

        if (children.isNotEmpty()) {
            throw BusinessRuleException(
                "Culture '${culture.name}' has ${children.size} descendant(s). Retire it instead of deleting it."
            )
        }

        cultureRepository.delete(culture)
    }

    fun getEntity(id: UUID): Culture =
        cultureRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("Culture", id) }
}
