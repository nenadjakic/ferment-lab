package com.github.nenadjakic.fermentlab.service

import com.github.nenadjakic.fermentlab.common.dto.reference.FermentTypeResponse
import com.github.nenadjakic.fermentlab.common.exception.ResourceNotFoundException
import com.github.nenadjakic.fermentlab.common.extension.toResponse
import com.github.nenadjakic.fermentlab.data.entity.reference.FermentType
import com.github.nenadjakic.fermentlab.data.repository.FermentTypeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Read access to the ferment type reference data.
 */
@Service
@Transactional(readOnly = true)
class FermentTypeService(
    private val fermentTypeRepository: FermentTypeRepository
) {

    fun findAll(): List<FermentTypeResponse> =
        fermentTypeRepository.findAllByOrderByNameAsc().map { it.toResponse() }

    fun findById(id: UUID): FermentTypeResponse =
        getEntity(id).toResponse()

    /**
     * Loads the entity for use by other services, raising 404 semantics when it is missing.
     */
    fun getEntity(id: UUID): FermentType =
        fermentTypeRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("Ferment type", id) }
}
