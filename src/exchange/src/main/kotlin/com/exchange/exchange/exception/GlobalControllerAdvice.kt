package com.exchange.exchange.exception

import com.exchange.exchange.core.Response
import kotlinx.coroutines.reactor.mono
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 *
 * @author uuhnaut69
 *
 */
@RestControllerAdvice
class GlobalControllerAdvice {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(GlobalControllerAdvice::class.java)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(ex: BadRequestException) = mono {
        ResponseEntity.badRequest().body(Response.fail(listOf(ex.message)))
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnAuthorizedException(ex: UnauthorizedException) = mono {
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Response.fail(listOf(ex.message)))
    }

    @ExceptionHandler(InternalServerErrorException::class, Exception::class)
    fun fallbackHandleException(ex: Exception) = mono {
        LOGGER.error(ex.message, ex)
        ResponseEntity.internalServerError().body(Response.fail(listOf(ex.message ?: "UNKNOWN_ERROR")))
    }

}
