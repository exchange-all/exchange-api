package com.exchange.exchange.core

/**
 * Format of external event when consume from kafka
 *
 * @author uuhnaut69
 *
 */
class ReplyEvent(
    val id: String?,
    val event: Any,
    val data: Any?,
    val error: String? = null,
)
