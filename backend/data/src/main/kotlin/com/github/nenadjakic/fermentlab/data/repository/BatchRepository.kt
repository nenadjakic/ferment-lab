package com.github.nenadjakic.fermentlab.data.repository

import com.github.nenadjakic.fermentlab.data.entity.batch.Batch
import com.github.nenadjakic.fermentlab.data.enum.BatchStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface BatchRepository : JpaRepository<Batch, UUID> {

    fun findAllByStatusInOrderByStartedOnDesc(statuses: Collection<BatchStatus>): List<Batch>

    fun countByStatus(status: BatchStatus): Long

    fun existsByCode(code: String): Boolean

    fun findAllByCodeStartingWith(prefix: String): List<Batch>

    /**
     * Paged batch search. Every filter is optional: passing `null` disables that predicate.
     *
     * @param search matched case insensitively against the batch name and code.
     */
    @Query(
        """
        SELECT b FROM Batch b
        WHERE (:search IS NULL
                OR LOWER(b.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(b.code) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
          AND (:fermentTypeId IS NULL OR b.fermentType.id = :fermentTypeId)
          AND (:status IS NULL OR b.status = :status)
        """
    )
    fun search(
        @Param("search") search: String?,
        @Param("fermentTypeId") fermentTypeId: UUID?,
        @Param("status") status: BatchStatus?,
        pageable: Pageable
    ): Page<Batch>

    /**
     * Number of batches per ferment type, used by the dashboard distribution chart.
     *
     * @return rows of `[fermentTypeName, count]`.
     */
    @Query(
        """
        SELECT b.fermentType.name, COUNT(b)
        FROM Batch b
        GROUP BY b.fermentType.name
        ORDER BY COUNT(b) DESC
        """
    )
    fun countGroupedByFermentType(): List<Array<Any>>

    /**
     * Average rating across all rated batches, `null` when nothing has been rated yet.
     */
    @Query("SELECT AVG(b.rating) FROM Batch b WHERE b.rating IS NOT NULL")
    fun averageRating(): Double?
}
