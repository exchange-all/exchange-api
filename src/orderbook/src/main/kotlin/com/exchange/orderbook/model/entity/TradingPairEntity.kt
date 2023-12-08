package com.exchange.orderbook.model.entity

import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
@Document("trading_pairs")
@CompoundIndexes(
    CompoundIndex(name = "pair_index", def = "{'baseCurrency': 1, 'quoteCurrency': 1}", unique = true)
)
class TradingPairEntity : Identifiable<UUID> {
    override lateinit var id: UUID
    lateinit var baseCurrency: String
    lateinit var quoteCurrency: String
    var minLimit: BigDecimal = BigDecimal.ZERO
}
