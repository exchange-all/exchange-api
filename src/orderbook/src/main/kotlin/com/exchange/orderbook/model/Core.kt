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
     * Min heap, sell order with the lowest price will be on top
     */
    val asks: PriorityQueue<Price> = PriorityQueue(Comparator.naturalOrder())
    val sellOrders: MutableMap<Price, TreeSet<OrderEntity>> = HashMap()

    /**
     * Max heap, buy order with the highest price will be on top
     */
    val bids: PriorityQueue<Price> = PriorityQueue(Comparator.reverseOrder())
    val buyOrders: MutableMap<Price, TreeSet<OrderEntity>> = HashMap()

    /**
     * Add order
     *
     * @param order
     */
    fun addOrder(order: OrderEntity) {
        if (order.type == OrderType.SELL) {
            this.asks.add(Price(order.price))
            this.sellOrders.putIfAbsent(Price(order.price), TreeSet(OrderPriority()))!!.add(order)
        }
        if (order.type == OrderType.BUY) {
            this.bids.add(Price(order.price))
            this.buyOrders.putIfAbsent(Price(order.price), TreeSet(OrderPriority()))!!.add(order)
        }
    }
}
