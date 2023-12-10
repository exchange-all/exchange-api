package com.exchange.orderbook.model.event

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


/**
 *
 * @author uuhnaut69
 *
 */
object CloudEventUtils {

    const val EVENT_SOURCE = "order-book-service"
    private val mapper = jacksonObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    fun serializeData(data: Any?): ByteArray? {
        return data?.let { this.mapper.writeValueAsBytes(it) }
    }

}
