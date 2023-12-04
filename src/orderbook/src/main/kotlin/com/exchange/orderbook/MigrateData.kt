package com.exchange.orderbook

import com.exchange.orderbook.model.entity.OffsetEntity
import com.exchange.orderbook.repository.disk.OffsetRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * @author thaivc
 * @since 2023
 */
@Component
class MigrateData(private val offsetRepository: OffsetRepository) {
  @Value("\${order-book.topic}") private lateinit var orderBookTopic: String

  companion object {
    private val log = LoggerFactory.getLogger(MigrateData::class.java)
  }

  fun initData() {
    checkAndMigrateOffset()
  }

  fun checkAndMigrateOffset() {
    val offsetEntity = offsetRepository.getOrderBookOffset()
    if (offsetEntity == null) {
      val newOffsetEntity =
          OffsetEntity().apply {
            id = "ORDER_BOOK"
            partition = 0
            topic = orderBookTopic
            offset = 0
          }
      offsetRepository.save(newOffsetEntity)
      log.info("INIT OFFSET ENTITY: $newOffsetEntity")
    }
  }
}
