package com.exchange.orderbook.model.event

import java.math.BigDecimal

/**
 * @author thaivc
 * @since 2023
 */
data class CreateBalanceEvent(
    override val id: String,
    val userId: String,
    val currency: String
) : IEvent {}

data class DepositBalanceEvent(
    override val id: String,
    val userId: String,
    val currency: String,
    val amount: BigDecimal
) : IEvent {}

data class WithdrawBalanceEvent(
    override val id: String,
    val userId: String,
    val currency: String,
    val amount: BigDecimal
) : IEvent {}

data class AskLimitOrderEvent(
    override val id: String,
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val price: BigDecimal,
    val amount: BigDecimal
) : IEvent {}

data class AskMarketOrderEvent(
    override val id: String,
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val amount: BigDecimal
) : IEvent {}

data class BidLimitOrderEvent(
    override val id: String,
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val price: BigDecimal,
    val amount: BigDecimal
) : IEvent {}

data class BidMarketOrderEvent(
    override val id: String,
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val amount: BigDecimal
) : IEvent {}

data class CancelOrderEvent(
    override val id: String,
    val userId: String,
    val orderId: String
) : IEvent {}
