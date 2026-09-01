package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.reference.FermentType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FermentTypeRepository : JpaRepository<FermentType, UUID> {

    fun findByCode(code: String): FermentType?

    fun findAllByOrderByNameAsc(): List<FermentType>
}
