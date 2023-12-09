package com.exchange.orderbook.service

import com.exchange.orderbook.model.Tuple
import com.exchange.orderbook.model.constants.HeaderType
import com.exchange.orderbook.model.event.EventResponse
import com.exchange.orderbook.model.event.EventResponseType
import com.exchange.orderbook.model.event.SuccessResponse
import com.exchange.orderbook.model.event.TradingResult
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Headers
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

/**
 * @author thaivc
 * @since 2023
 */
@Service
class EventOutboundHandler(private val kafkaTemplate: KafkaTemplate<String, Any>) {

    @Value("\${order-book.reply-topic}")
    private lateinit var replyOrderBookTopic: String

    fun publishEvent(responses: List<Tuple<EventResponse, Headers>>) {
        responses
            .map {
                // ignore response for the request that does not have CE_TYPE header
                if (it.first !is TradingResult && it.second?.headers(HeaderType.CE_TYPE)?.firstOrNull() == null) {
                    return
                }

                ProducerRecord<String, Any>(replyOrderBookTopic, it.first!!.id, it.first).apply {
                    if (it.second == null) { // check TradingResult
                        headers().add(HeaderType.CE_TYPE, EventResponseType.TRADING_RESULT.toByteArray())
                    } else {
                        it.second.forEach { header -> headers().add(header.key(), header.value()) }
                        val eventType = String(it.second.headers(HeaderType.CE_TYPE).first().value())
                        val ceType =
                            if (it.first is SuccessResponse)
                                EventResponseType.success(eventType)
                            else
                                EventResponseType.fail(eventType)
                        headers().add(HeaderType.CE_TYPE, ceType.toByteArray())
                    }
                }
            }
            .forEach {
                println("Publishing event: ${it.value()}")
                kafkaTemplate.send(it)
            }
    }
}
