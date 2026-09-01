package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.culture.Culture
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CultureRepository : JpaRepository<Culture, UUID> {

    fun findAllByOrderByNameAsc(): List<Culture>

    fun findAllByActiveTrueOrderByNameAsc(): List<Culture>

    fun findAllByParentIdOrderByNameAsc(parentId: UUID): List<Culture>

    fun countByActiveTrue(): Long

    /**
     * Cultures that are fed on a schedule. The next feed date is derived in memory from
     * [Culture.nextFeedDueOn] by the reminder job.
     */
    fun findAllByActiveTrueAndFeedIntervalDaysNotNull(): List<Culture>
}
