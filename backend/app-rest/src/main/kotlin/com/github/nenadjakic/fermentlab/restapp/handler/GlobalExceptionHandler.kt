package com.github.nenadjakic.fermentlab.restapp.handler

import com.github.nenadjakic.fermentlab.common.exception.BusinessRuleException
import com.github.nenadjakic.fermentlab.common.exception.ResourceNotFoundException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates exceptions into [ErrorInfo] bodies with the right status code.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(
        exception: ResourceNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorInfo> = build(HttpStatus.NOT_FOUND, exception.message, request)

    @ExceptionHandler(BusinessRuleException::class)
    fun handleBusinessRule(
        exception: BusinessRuleException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorInfo> = build(HttpStatus.CONFLICT, exception.message, request)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBodyValidation(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorInfo> {
        val fieldErrors = exception.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "is invalid")
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorInfo(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = "Request validation failed.",
                    path = request.requestURI,
                    fieldErrors = fieldErrors
                )
            )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleParameterValidation(
        exception: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorInfo> {
        val fieldErrors = exception.constraintViolations.associate {
            it.propertyPath.toString() to it.message
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorInfo(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = "Request validation failed.",
                    path = request.requestURI,
                    fieldErrors = fieldErrors
                )
            )
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(
        exception: DataIntegrityViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorInfo> {
        logger.warn("Data integrity violation on {}", request.requestURI, exception)
        return build(HttpStatus.CONFLICT, "The request conflicts with existing data.", request)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorInfo> {
        logger.error("Unhandled error on {}", request.requestURI, exception)
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request)
    }

    private fun build(
        status: HttpStatus,
        message: String?,
        request: HttpServletRequest
    ): ResponseEntity<ErrorInfo> =
        ResponseEntity
            .status(status)
            .body(
                ErrorInfo(
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = message ?: status.reasonPhrase,
                    path = request.requestURI
                )
            )
}
