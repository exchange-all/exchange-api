package com.exchange.orderbook.repository.memory

import com.exchange.orderbook.model.currencyPair
import com.exchange.orderbook.model.entity.TradingPairEntity
import org.springframework.stereotype.Repository
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
@Repository
class TradingPairInMemoryRepository {
    val data: MutableMap<String, TradingPairEntity> = HashMap()

    val pairMap: MutableMap<String, String> = HashMap()

    fun upsert(item: TradingPairEntity) {
        data[item.id] = item
        pairMap[currencyPair(item.baseCurrency, item.quoteCurrency)] = item.id
    }

    fun findById(id: String): TradingPairEntity? {
        return data[id]
    }

    fun existByCurrencyPair(baseCurrency: String, quoteCurrency: String): Boolean {
        return pairMap.containsKey(currencyPair(baseCurrency, quoteCurrency))
    }

    fun findByCurrencyPair(baseCurrency: String, quoteCurrency: String): TradingPairEntity? {
        return pairMap[currencyPair(baseCurrency, quoteCurrency)]?.let { data[it] }
    }
}
