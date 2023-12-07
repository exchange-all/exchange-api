package com.exchange.exchange.core

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.CloudEventUtils.mapData
import io.cloudevents.jackson.PojoCloudEventDataMapper


/**
 *
 * @author uuhnaut69
 *
 */
object CloudEventUtils {

    const val EVENT_SOURCE = "exchange-service"

    fun serializeData(data: Any): ByteArray {
        return jacksonObjectMapper().writeValueAsBytes(data)
    }

    fun <T> cloudEventToObject(cloudEvent: CloudEvent, clazz: Class<T>): T? {
        val cloudEventData = mapData(
                cloudEvent, PojoCloudEventDataMapper.from(jacksonObjectMapper(), clazz)
        )
        return cloudEventData?.value
    }

    fun <T> getExternalEventData(cloudEvent: CloudEvent, clazz: Class<T>): T? {
        val cloudEventData = mapData(
                cloudEvent,
                PojoCloudEventDataMapper.from(jacksonObjectMapper(), ExternalEvent::class.java)
        )
        return cloudEventData?.value?.data?.let {
            jacksonObjectMapper().convertValue(it, clazz)
        }
    }

    fun getExternalEventError(cloudEvent: CloudEvent): String? {
        val cloudEventData = mapData(
                cloudEvent,
                PojoCloudEventDataMapper.from(jacksonObjectMapper(), ExternalEvent::class.java)
        )
        return cloudEventData?.value?.error
    }

}
