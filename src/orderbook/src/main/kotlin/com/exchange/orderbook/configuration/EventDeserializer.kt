package com.exchange.orderbook.configuration

import com.exchange.orderbook.model.event.IEvent
import com.exchange.orderbook.model.event.UnknownEvent
import com.exchange.orderbook.model.event.typeOf
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Deserializer

/**
 * @author thaivc
 * @since 2023
 */
class EventDeserializer : Deserializer<IEvent> {

  override fun deserialize(topic: String?, headers: Headers?, data: ByteArray?): IEvent {
    val eventType = headers?.headers("type")?.firstOrNull()?.value()?.let {
      String(it)
    } ?: return UnknownEvent.DEFAULT

    return typeOf(eventType)?.let { jacksonObjectMapper().readValue(data, it) } ?: UnknownEvent.DEFAULT
  }

  override fun deserialize(topic: String?, data: ByteArray?): IEvent {
    return UnknownEvent.DEFAULT
  }
}
