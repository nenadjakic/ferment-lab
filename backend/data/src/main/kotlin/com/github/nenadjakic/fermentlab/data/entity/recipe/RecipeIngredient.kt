package com.github.nenadjakic.fermentlab.data.entity.recipe

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

/**
 * One line of a recipe's ingredient list.
 */
@Entity
@Table(name = "recipe_ingredients")
data class RecipeIngredient(

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    val recipe: Recipe,

    /**
     * Ordering within the ingredient list, starting at 1.
     */
    @Column(name = "ordinal", nullable = false)
    val position: Int,

    /**
     * Ingredient name, e.g. "Napa cabbage".
     */
    @Column(name = "name", length = 120, nullable = false)
    val name: String,

    /**
     * Quantity needed for the recipe's base yield.
     */
    @Column(name = "quantity", precision = 10, scale = 3)
    val quantity: BigDecimal? = null,

    /**
     * Unit of [quantity].
     */
    @Column(name = "unit", length = 15)
    val unit: String? = null,

    /**
     * Optional preparation hint, e.g. "coarsely grated".
     */
    @Column(name = "note", length = 255)
    val note: String? = null
) {
    @PrePersist
    fun prePersist() {
        if (id == null) {
            id = UUID.randomUUID()
        }
    }
}
