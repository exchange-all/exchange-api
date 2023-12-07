package com.exchange.orderbook.repository.disk

import com.exchange.orderbook.model.entity.OrderEntity
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
interface OrderRepository : MongoRepository<OrderEntity, UUID> {}
