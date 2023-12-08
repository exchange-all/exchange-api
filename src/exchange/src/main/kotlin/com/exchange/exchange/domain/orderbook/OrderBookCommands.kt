package com.exchange.exchange.domain.orderbook

import java.math.BigDecimal

/**
 * @author uuhnaut69
 *
 */
data class CreateAskLimitOrderCommand(
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val price: BigDecimal,
    val amount: BigDecimal
)

data class CreateBidLimitOrderCommand(
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val price: BigDecimal,
    val amount: BigDecimal
)
