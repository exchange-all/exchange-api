package com.exchange.exchange.core

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
        return ObjectMapper.instance.writeValueAsBytes(data)
    }

    fun <T> cloudEventToObject(cloudEvent: CloudEvent, clazz: Class<T>): T? {
        val cloudEventData = mapData(
            cloudEvent, PojoCloudEventDataMapper.from(ObjectMapper.instance, clazz)
        )
        return cloudEventData?.value
    }

    fun <T> getReplyEventData(cloudEvent: CloudEvent, clazz: Class<T>): T? {
        val cloudEventData = mapData(
            cloudEvent, PojoCloudEventDataMapper.from(ObjectMapper.instance, ReplyEvent::class.java)
        )
        return cloudEventData?.value?.data?.let {
            ObjectMapper.instance.convertValue(it, clazz)
        }
    }

    fun getReplyEventError(cloudEvent: CloudEvent): String? {
        val cloudEventData = mapData(
            cloudEvent, PojoCloudEventDataMapper.from(ObjectMapper.instance, ReplyEvent::class.java)
        )
        return cloudEventData?.value?.error
    }

}
