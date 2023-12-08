package com.exchange.orderbook.repository.disk

import com.exchange.orderbook.model.entity.OrderEntity
import org.springframework.data.mongodb.repository.MongoRepository

/**
 * @author thaivc
 * @since 2023
 */
interface OrderRepository : MongoRepository<OrderEntity, String> {}
