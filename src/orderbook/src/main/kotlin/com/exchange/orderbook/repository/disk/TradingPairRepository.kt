package com.exchange.orderbook.repository.disk

import com.exchange.orderbook.model.entity.TradingPairEntity
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
interface TradingPairRepository : MongoRepository<TradingPairEntity, UUID> {
    fun findFirstByBaseCurrencyAndQuoteCurrency(baseCurrency: String, quoteCurrency: String): TradingPairEntity?
}
