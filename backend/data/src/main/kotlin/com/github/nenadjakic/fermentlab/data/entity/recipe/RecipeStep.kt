package com.github.nenadjakic.fermentlab.data.entity.recipe

import com.github.nenadjakic.fermentlab.data.enum.StagePhase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.util.UUID

/**
 * One instruction of a recipe. Steps double as the template for a batch's stages: starting a batch
 * from a recipe copies every step into a [com.github.nenadjakic.fermentlab.data.entity.batch.BatchStage].
 */
@Entity
@Table(name = "recipe_steps")
data class RecipeStep(

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    val recipe: Recipe,

    /**
     * Ordering within the method, starting at 1.
     */
    @Column(name = "ordinal", nullable = false)
    val position: Int,

    /**
     * Short label for the step, e.g. "Primary ferment".
     */
    @Column(name = "title", length = 150, nullable = false)
    val title: String,

    /**
     * Full instruction text.
     */
    @Column(name = "instruction", length = 2000, nullable = false)
    val instruction: String,

    /**
     * Fermentation phase the step belongs to.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "phase", length = 20, nullable = false)
    val phase: StagePhase = StagePhase.PRIMARY,

    /**
     * How long the step takes, in days. Zero for immediate actions.
     */
    @Column(name = "duration_days", nullable = false)
    val durationDays: Int = 0
) {
    @PrePersist
    fun prePersist() {
        if (id == null) {
            id = UUID.randomUUID()
        }
    }
}
