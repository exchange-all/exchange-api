package com.exchange.orderbook.model.entity

import java.math.BigDecimal
import java.util.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * @author thaivc
 * @since 2023
 */
@Document("orders")
class OrderEntity : Identifiable<UUID>, Cloneable {
  @Id override lateinit var id: UUID
  lateinit var userId: UUID
  lateinit var tradingPairId: UUID
  lateinit var amount: BigDecimal
  lateinit var price: BigDecimal
  lateinit var type: String

  override fun clone(): OrderEntity {
    TODO("Not implement yet")
  }
}
