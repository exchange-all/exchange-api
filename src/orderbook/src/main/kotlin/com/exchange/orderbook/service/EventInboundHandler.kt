package com.exchange.orderbook.service

import com.exchange.orderbook.model.event.IEvent
import com.exchange.orderbook.repository.disk.OffsetRepository
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * @author thaivc
 * @since 2023
 */
@Service
class EventInboundHandler(
    private val offsetRepository: OffsetRepository,
    private val orderBookConsumerProvider: (Int) -> KafkaConsumer<String, IEvent>,
    private val coreEngine: CoreEngine
) {
  @Value("\${order-book.topic}") private lateinit var orderBookTopic: String
  @Value("\${order-book.consume-poll-size}") private var consumePollSize: Int = 10_000
  @Value("\${order-book.consume-poll-duration}") private var consumePollDuration: Long = 50

  fun pollingMessage() {
    val offsetEntity = offsetRepository.getOrderBookOffset()
    orderBookConsumerProvider(consumePollSize).use {
      val topicPartition = TopicPartition(orderBookTopic, offsetEntity?.partition ?: 0)
      it.assign(listOf(topicPartition))
      it.seek(topicPartition, offsetEntity?.offset ?: 0)
      while (true) {
        val records = it.poll(Duration.ofMillis(consumePollDuration))
        coreEngine.consumeEvents(records)
        it.commitSync()
      }
    }
  }
}
