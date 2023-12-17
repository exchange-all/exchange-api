package com.exchange.orderbook.service

import com.exchange.orderbook.model.constants.HeaderType
import com.exchange.orderbook.model.event.*
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Headers
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.net.URI
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
@Service
class EventOutboundHandler(private val kafkaTemplate: KafkaTemplate<String, CloudEvent>) {

    @Value("\${order-book.reply-topic}")
    private lateinit var replyOrderBookTopic: String

    fun publishEvent(responses: List<Pair<ExchangeEvent, Headers>>) {
        responses
            .filter {
                // Only publish an event if it is a success response or a fail response with CORRELATION_ID header
                it.first !is NotResponse &&
                        !(it.first is FailResponse && it.second.headers(HeaderType.CORRELATION_ID)
                            ?.firstOrNull() == null)
            }
            .map {
                val id = UUID.randomUUID().toString()
                val eventBuilder = CloudEventBuilder.v1()
                    .withId(id)
                    .withSource(URI.create(CloudEventUtils.EVENT_SOURCE))
                    .withData(CloudEventUtils.serializeData(it.first))

                when (it.first) {
                    is BalanceChangedEvent -> {
                        eventBuilder.withType(EventResponseType.BALANCE_CHANGED)
                    }
                    is OrderChangedEvent -> {
                        eventBuilder.withType(EventResponseType.ORDER_CHANGED)
                    }
                    is TradingResult -> {
                        eventBuilder.withType(EventResponseType.TRADING_RESULT)
                    }
                    is EventResponse -> {
                        val requestEventType = String(it.second.headers(HeaderType.CE_TYPE).first().value())
                        val replyEventType =
                            if (it.first is SuccessResponse)
                                EventResponseType.success(requestEventType)
                            else
                                EventResponseType.fail(requestEventType)
                        eventBuilder.withType(replyEventType)
                    }
                }

                val event = eventBuilder.build()

                return@map ProducerRecord<String, CloudEvent>(this.replyOrderBookTopic, event.id, event)
                    .apply {
                        it.second.forEach { header ->
                            this.headers().add(header)
                        }

                        // replace CE_TYPE header
                        it.second.add(HeaderType.CE_TYPE, event.type.toByteArray())

                        if (it.first is SideEffectEvent) {
                            this.headers().remove(HeaderType.REPLY_TOPIC)
                        }
                    }
            }
            .forEach { kafkaTemplate.send(it) }
    }
}
