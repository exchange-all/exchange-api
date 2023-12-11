package com.exchange.orderbook.model

import com.exchange.orderbook.model.entity.OrderEntity
import com.exchange.orderbook.model.entity.OrderType
import com.exchange.orderbook.model.entity.Status
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
    val asks: TreeMap<Price, TreeSet<OrderEntity>> = TreeMap(Comparator.naturalOrder())

    /**
     * Buy order with the highest price will be on top
     */
    val bids: TreeMap<Price, TreeSet<OrderEntity>> = TreeMap(Comparator.reverseOrder())

    /**
     * Order reference, that is used to cancel order
     */
    val orderRefs: MutableMap<String, OrderEntity> = HashMap()

    /**
     * Order tree reference, that is used to cancel order
     */
    val orderTreeRefs: MutableMap<String, TreeSet<OrderEntity>> = HashMap()

    /**
     * Add order
     *
     * @param order
     */
    fun addOrder(order: OrderEntity) {
        this.orderRefs[order.id] = order
        if (order.type == OrderType.SELL) {
            this.asks.computeIfAbsent(Price(order.price)) { TreeSet(OrderPriority()) }.add(order)
            this.orderTreeRefs[order.id] = this.asks[Price(order.price)]!!
        }
        if (order.type == OrderType.BUY) {
            this.bids.computeIfAbsent(Price(order.price)) { TreeSet(OrderPriority()) }.add(order)
            this.orderTreeRefs[order.id] = this.bids[Price(order.price)]!!
        }
    }
}
