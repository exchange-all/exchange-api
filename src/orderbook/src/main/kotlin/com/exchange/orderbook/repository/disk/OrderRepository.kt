package com.exchange.orderbook.repository.disk

import com.exchange.orderbook.model.entity.OrderEntity
import com.exchange.orderbook.model.entity.Status
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.repository.MongoRepository

/**
 * @author thaivc
 * @since 2023
 */
interface OrderRepository : MongoRepository<OrderEntity, String> {
    fun findByStatus(status: Status, sort: Sort): List<OrderEntity>
}
