package com.exchange.orderbook.repository.disk

import com.exchange.orderbook.model.entity.BalanceEntity
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
interface BalanceRepository : MongoRepository<BalanceEntity, UUID> {
}
