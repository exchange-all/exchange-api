package com.exchange.exchange.core

/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
data class Response<T>(
    val success: Boolean,
    val data: T? = null,
    val errors: List<ErrorResponse> = mutableListOf(),
) {
    companion object {
        fun success(): Response<Unit> {
            return Response(true)
        }

        fun <T> success(data: T): Response<T> {
            return Response(true, data)
        }

        fun errors(errors: List<ErrorResponse>): Response<Unit> {
            return Response(false, errors = errors)
        }
    }
}

data class ErrorResponse(
    val errorName: String,
    val message: String,
    val constraints: Map<String, Any>? = null,
)
