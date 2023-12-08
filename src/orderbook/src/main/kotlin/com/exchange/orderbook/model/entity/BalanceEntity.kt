package com.exchange.orderbook.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

/**
 * @author thaivc
 * @since 2023
 */
@Document("balances")
class BalanceEntity : Identifiable<String>, Cloneable {
    @Id
    override lateinit var id: String

    lateinit var userId: String
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
