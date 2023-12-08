package com.exchange.exchange.core

/**
 *
 * @author uuhnaut69
 *
 */
data class Response<T>(
    val success: Boolean,
    val data: T? = null,
    val errors: List<String>? = null,
) {
    companion object {
        fun success(): Response<Unit> {
            return Response(true)
        }

        fun <T> success(data: T): Response<T> {
            return Response(true, data)
        }

        fun fail(errors: List<String>): Response<Unit> {
            return Response(false, errors = errors)
        }
    }
}
