package com.exchange.orderbook.service

import com.exchange.orderbook.model.Tuple
import com.exchange.orderbook.model.constants.HeaderType
import com.exchange.orderbook.model.event.*
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Headers
import org.slf4j.LoggerFactory
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

    companion object {
        private val LOGGER = LoggerFactory.getLogger(EventOutboundHandler::class.java)
    }

    @Value("\${order-book.reply-topic}")
    private lateinit var replyOrderBookTopic: String

    fun publishEvent(responses: List<Tuple<ExchangeEvent, Headers>>) {
        responses
            .filter {
                // Only publish an event if it is a success response or a fail response with CE_TYPE header
                it.first is SuccessResponse
                        || !(it.first is FailResponse && it.second?.headers(HeaderType.CE_TYPE)?.firstOrNull() == null)
            }
            .map {
                val id = UUID.randomUUID().toString()
                val eventBuilder = CloudEventBuilder.v1()
                    .withId(id)
                    .withSource(URI.create(CloudEventUtils.EVENT_SOURCE))
                    .withData(CloudEventUtils.serializeData(it.first))

                // Build cloud event data
                if (it.second == null) { // check TradingResult
                    eventBuilder.withType(EventResponseType.TRADING_RESULT)
                } else {
                    val requestEventType = String(it.second.headers(HeaderType.CE_TYPE).first().value())
                    val replyEventType =
                        if (it.first is SuccessResponse)
                            EventResponseType.success(requestEventType)
                        else
                            EventResponseType.fail(requestEventType)
                    eventBuilder.withType(replyEventType)
                }

                val event = eventBuilder.build()

                return@map ProducerRecord<String, CloudEvent>(this.replyOrderBookTopic, event.id, event)
                    .apply {
                        // Keep all original headers except CE_TYPE due to already set in CE type
                        it.second?.filter { header -> header.key() != HeaderType.CE_TYPE }
                            ?.forEach { header -> this.headers().add(header) }
                    }
            }
            .forEach {
                kafkaTemplate.send(it)
                LOGGER.info("$it")
            }
    }
}
