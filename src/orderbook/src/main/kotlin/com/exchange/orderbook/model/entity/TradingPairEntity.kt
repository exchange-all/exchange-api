package com.exchange.orderbook.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

/**
 * @author thaivc
 * @since 2023
 */
@Document("trading_pairs")
@CompoundIndexes(
    CompoundIndex(name = "pair_index", def = "{'baseCurrency': 1, 'quoteCurrency': 1}", unique = true)
)
class TradingPairEntity : Identifiable<String> {
    @Id
    override lateinit var id: String
    lateinit var baseCurrency: String
    lateinit var quoteCurrency: String
    var minLimit: BigDecimal = BigDecimal.ZERO
}
