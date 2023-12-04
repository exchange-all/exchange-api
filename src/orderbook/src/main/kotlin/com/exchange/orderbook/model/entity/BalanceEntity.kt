package com.exchange.orderbook.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
@Document("balances")
class BalanceEntity : Identifiable<UUID>, Cloneable {
  @Id
  override lateinit var id: UUID

  lateinit var userId: UUID
  lateinit var currency: String
  lateinit var availableAmount: BigDecimal
  lateinit var lockAmount: BigDecimal
  override fun clone(): BalanceEntity {
    val cloned = BalanceEntity()
    cloned.id = this.id
    cloned.userId = this.userId
    cloned.currency = this.currency
    cloned.availableAmount = this.availableAmount
    cloned.lockAmount = this.lockAmount
    return cloned
  }
}
