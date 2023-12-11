package com.exchange.exchange.domain.orderbook

import java.math.BigDecimal

/**
 * @author uuhnaut69
 *
 */
data class CreateAskLimitOrderCommand(
    val id: String,
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val price: BigDecimal,
    val amount: BigDecimal
)

data class CreateBidLimitOrderCommand(
    val id: String,
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val price: BigDecimal,
    val amount: BigDecimal
)

data class CancelOrderCommand(
    val id: String,
    val userId: String,
    val orderId: String
)
