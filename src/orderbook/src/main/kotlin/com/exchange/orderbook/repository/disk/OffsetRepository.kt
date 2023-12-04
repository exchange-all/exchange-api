package com.exchange.orderbook.repository.disk

import com.exchange.orderbook.model.entity.OffsetEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

/**
 * @author thaivc
 * @since 2023
 */
interface OffsetRepository : MongoRepository<OffsetEntity, String> {
  @Query(value = "{ 'id': 'ORDER_BOOK' }")
  fun getOrderBookOffset(): OffsetEntity?
}
