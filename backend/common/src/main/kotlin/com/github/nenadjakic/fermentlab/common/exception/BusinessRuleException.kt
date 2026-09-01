package com.github.nenadjakic.fermentlab.common.exception

/**
 * Thrown when a request is well formed but not allowed in the current state of the data, for example
 * completing a batch that is already closed. Mapped to HTTP 409.
 */
class BusinessRuleException(message: String) : RuntimeException(message)
