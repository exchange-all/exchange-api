package com.exchange.orderbook.configuration

import com.exchange.orderbook.model.constants.HeaderType
import com.exchange.orderbook.model.event.IEvent
import com.exchange.orderbook.model.event.UnknownEvent
import com.exchange.orderbook.model.event.UnknownFormatEvent
import com.exchange.orderbook.model.event.typeOf
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Deserializer

/**
 * @author thaivc
 * @since 2023
 */
class EventDeserializer : Deserializer<IEvent> {

    override fun deserialize(topic: String?, headers: Headers?, data: ByteArray?): IEvent {
        try {
            val eventType = headers?.headers(HeaderType.CE_TYPE)?.firstOrNull()?.value()?.let {
                String(it)
            } ?: return UnknownEvent.DEFAULT

            return typeOf(eventType)?.let { jacksonObjectMapper().readValue(data, it) } ?: UnknownFormatEvent.DEFAULT
        } catch (e: Exception) {
            return UnknownFormatEvent.of(e.message)
        }
    }

    override fun deserialize(topic: String?, data: ByteArray?): IEvent {
        return UnknownEvent.DEFAULT
    }
}
