package com.exchange.exchange.exception

import com.exchange.exchange.core.ErrorResponse
import com.exchange.exchange.core.Response
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
@RestControllerAdvice
class GlobalControllerAdvice {

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(ex: BadRequestException) = mono {
        val errors = listOf(
            ErrorResponse(
                errorName = "BAD_REQUEST_ERROR",
                message = ex.message,
                constraints = mutableMapOf(),
            )
        )
        ResponseEntity.badRequest().body(Response.errors(errors))
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnAuthorizedException(ex: UnauthorizedException) = mono {
        val errors = listOf(
            ErrorResponse(
                errorName = "UNAUTHORIZED_ERROR",
                message = ex.message,
                constraints = mutableMapOf(),
            )
        )
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Response.errors(errors))
    }

    @ExceptionHandler(Exception::class)
    fun fallbackHandleException(ex: Exception) = mono {
        val errors = listOf(
            ErrorResponse(
                errorName = "INTERNAL_SERVER_ERROR",
                message = ex.message ?: "UNKNOWN_ERROR",
                constraints = mutableMapOf(),
            )
        )
        ResponseEntity.internalServerError().body(Response.errors(errors))
    }

}
