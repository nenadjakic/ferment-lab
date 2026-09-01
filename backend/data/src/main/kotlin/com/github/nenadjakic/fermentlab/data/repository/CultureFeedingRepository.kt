package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.culture.CultureFeeding
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CultureFeedingRepository : JpaRepository<CultureFeeding, UUID> {

    fun findAllByCultureIdOrderByFedAtDesc(cultureId: UUID): List<CultureFeeding>
}
