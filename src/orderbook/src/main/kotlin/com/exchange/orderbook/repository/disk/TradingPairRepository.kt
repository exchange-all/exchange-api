package com.exchange.orderbook.repository.disk

import com.exchange.orderbook.model.entity.TradingPairEntity
import org.springframework.data.mongodb.repository.MongoRepository

/**
 * @author thaivc
 * @since 2023
 */
interface TradingPairRepository : MongoRepository<TradingPairEntity, String> {
    fun findFirstByBaseCurrencyAndQuoteCurrency(baseCurrency: String, quoteCurrency: String): TradingPairEntity?
}
