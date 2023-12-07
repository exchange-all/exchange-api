package com.exchange.exchange.exception

/**
 *
 * @author uuhnaut69
 *
 */
class BadRequestException(
        override val message: String = "BAD_REQUEST_ERROR",
) : RuntimeException()

class UnauthorizedException(
        override val message: String = "UNAUTHORIZED_ERROR",
) : RuntimeException()

class InternalServerErrorException(
        override val message: String = "INTERNAL_SERVER_ERROR",
) : RuntimeException()
