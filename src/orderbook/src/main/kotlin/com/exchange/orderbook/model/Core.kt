package com.exchange.orderbook.model

import com.exchange.orderbook.model.entity.OrderEntity
import com.exchange.orderbook.model.entity.OrderType
import java.math.BigDecimal
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */

/**
 * Convert to currency pair
 */
fun currencyPair(baseCurrency: String, quoteCurrency: String): String {
    return "${baseCurrency}/${quoteCurrency}"
}

/**
 * Price of order
 */
data class Price(val value: BigDecimal) : Comparable<Price> {
    override fun compareTo(other: Price): Int {
        return value.compareTo(other.value)
    }
}

/**
 * Priority of order having same price
 */
class OrderPriority : Comparator<OrderEntity> {
    override fun compare(thisOrder: OrderEntity, thatOrder: OrderEntity): Int {
        return thisOrder.priority.compareTo(thatOrder.priority)
    }
}

/**
 * Contains buy and sell orders of the trading-pair
 */
class TradingPair(
    var baseCurrency: String,
    var quoteCurrency: String
) {
    var key: String = currencyPair(this.baseCurrency, this.quoteCurrency)

    /**
     * Sell order with the lowest price will be on top
     */
    val asks: TreeMap<BigDecimal, LinkedHashMap<String, OrderEntity>> = TreeMap(Comparator.naturalOrder())

    /**
     * Buy order with the highest price will be on top
     */
    val bids: TreeMap<BigDecimal, LinkedHashMap<String, OrderEntity>> = TreeMap(Comparator.reverseOrder())

    /**
     * Add order
     *
     * @param order
     */
    fun addOrder(order: OrderEntity) {
        val bucket = when (order.type) {
            OrderType.SELL -> this.asks
            OrderType.BUY -> this.bids
        }
        val linkedHashMap = bucket.computeIfAbsent(order.price) { LinkedHashMap() }
        linkedHashMap[order.id] = order
    }

    fun removeOrder(order: OrderEntity) {
        val bucket = when (order.type) {
            OrderType.SELL -> this.asks
            OrderType.BUY -> this.bids
        }
        bucket[order.price]?.let {
            it.remove(order.id)
            if (it.isEmpty()) {
                bucket.remove(order.price)
            }
        }
    }
}
