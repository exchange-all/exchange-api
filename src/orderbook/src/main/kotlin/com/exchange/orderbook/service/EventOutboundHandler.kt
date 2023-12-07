package com.exchange.orderbook.service

import com.exchange.orderbook.model.event.EventResponse
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

/**
 * @author thaivc
 * @since 2023
 */
@Service
class EventOutboundHandler(private val kafkaTemplate: KafkaTemplate<String, Any>) {

  @Value("\${order-book.reply-topic}") private lateinit var replyOrderBookTopic: String

  fun publishEvent(responses: List<EventResponse>) {
    responses
        .map {
          ProducerRecord<String, Any>(replyOrderBookTopic, it.id, it).apply {
            headers().add("type", it::class.java.simpleName.toByteArray())
          }
        }
        .forEach { kafkaTemplate.send(it) }
  }
}
