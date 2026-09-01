package com.github.nenadjakic.fermentlab.common.exception

/**
 * Thrown when a record referenced by an identifier does not exist. Mapped to HTTP 404.
 */
class ResourceNotFoundException(message: String) : RuntimeException(message) {

    companion object {

        fun of(entity: String, id: Any): ResourceNotFoundException =
            ResourceNotFoundException("$entity with id $id was not found.")
    }
}
