package com.exchange.orderbook.model.entity

import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
@Document("trading_pairs")
class TradingPairEntity : Identifiable<UUID> {
  override lateinit var id: UUID
  lateinit var baseCurrency: String
  lateinit var quoteCurrency: String
}
