package com.exchange.orderbook.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.IndexDirection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

/**
 * @author thaivc
 * @since 2023
 */
@Document("orders")
class OrderEntity : Identifiable<String>, Cloneable {
    @Id
    override lateinit var id: String
    lateinit var userId: String
    lateinit var tradingPairId: String
    lateinit var amount: BigDecimal
    lateinit var availableAmount: BigDecimal
    lateinit var price: BigDecimal
    lateinit var type: OrderType
    lateinit var status: Status

    // The priority of the order. The higher the priority, the more likely the order will be matched.
    @Indexed(direction = IndexDirection.ASCENDING)
    var priority: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        fun sell(
            id: String,
            userId: String,
            tradingPairId: String,
            amount: BigDecimal,
            price: BigDecimal,
            offset: Long
        ): OrderEntity {
            return OrderEntity().apply {
                this.id = id
                this.userId = userId
                this.tradingPairId = tradingPairId
                this.amount = amount
                this.availableAmount = amount
                this.price = price
                this.type = OrderType.SELL
                this.status = Status.OPEN
                this.priority = offset
            }
        }

        fun buy(
            id: String,
            userId: String,
            tradingPairId: String,
            amount: BigDecimal,
            price: BigDecimal,
            offset: Long
        ): OrderEntity {
            return OrderEntity().apply {
                this.id = id
                this.userId = userId
                this.tradingPairId = tradingPairId
                this.amount = amount
                this.availableAmount = amount
                this.price = price
                this.type = OrderType.BUY
                this.status = Status.OPEN
                this.priority = offset
            }
        }
    }

    override fun clone(): OrderEntity {
        val cloned = OrderEntity()
        cloned.id = this.id
        cloned.tradingPairId = this.tradingPairId
        cloned.userId = this.userId
        cloned.amount = this.amount
        cloned.availableAmount = this.availableAmount
        cloned.price = this.price
        cloned.type = this.type
        cloned.status = this.status
        cloned.priority = this.priority
        return cloned
    }
}

enum class OrderType {
    BUY, SELL
}

enum class Status {
    OPEN, CLOSED, CANCEL
}
