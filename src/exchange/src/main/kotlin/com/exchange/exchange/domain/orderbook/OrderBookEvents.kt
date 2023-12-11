package com.exchange.exchange.domain.orderbook

import com.exchange.exchange.core.Event
import java.math.BigDecimal

/**
 * @author uuhnaut69
 *
 */
data class AskLimitOrderCreated(
    val id: String,
    val userId: String,
    val tradingPairId: String,
    val amount: BigDecimal,
    val availableAmount: BigDecimal,
    val price: BigDecimal,
    val type: String,
    val status: String,
) : Event()

data class BidLimitOrderCreated(
    val id: String,
    val userId: String,
    val tradingPairId: String,
    val amount: BigDecimal,
    val availableAmount: BigDecimal,
    val price: BigDecimal,
    val type: String,
    val status: String,
) : Event()

data class AskLimitOrderCancelled(
    val id: String,
    val userId: String,
    val tradingPairId: String,
    val amount: BigDecimal,
    val availableAmount: BigDecimal,
    val price: BigDecimal,
    val type: String,
    val status: String,
) : Event()

data class BidLimitOrderCancelled(
    val id: String,
    val userId: String,
    val tradingPairId: String,
    val amount: BigDecimal,
    val availableAmount: BigDecimal,
    val price: BigDecimal,
    val type: String,
    val status: String,
) : Event()
