package com.exchange.exchange.domain.market

import com.exchange.exchange.domain.orderbook.OrderSide
import com.exchange.exchange.domain.orderbook.OrderStatus
import java.math.BigDecimal

/**
 * @author uuhnaut69
 *
 */
data class WindowedTrade(
    val windowSize: WindowSize,
    val tradingPairId: String,
    val openPrice: BigDecimal,
    val closePrice: BigDecimal,
    val highPrice: BigDecimal,
    val lowPrice: BigDecimal,
    val timestamp: Long
)

data class TradingHistory(
    val userId: String,
    val tradingPairId: String,
    val orderId: String,
    val amount: BigDecimal,
    val availableAmount: BigDecimal,
    val price: BigDecimal,
    val type: OrderSide,
    val status: OrderStatus,
    val tradedAmount: BigDecimal,
    val tradedPrice: BigDecimal,
    val tradedAt: Long
)
