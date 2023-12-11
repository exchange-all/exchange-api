package com.exchange.orderbook.model.event

import java.math.BigDecimal

/**
 * @author thaivc
 * @since 2023
 */
class CreateBalanceEvent(
    override val id: String,
    val userId: String,
    val currency: String
) : IEvent {}

class DepositBalanceEvent(
    override val id: String,
    val userId: String,
    val currency: String,
    val amount: BigDecimal
) : IEvent {}

class WithdrawBalanceEvent(
    override val id: String,
    val userId: String,
    val currency: String,
    val amount: BigDecimal
) : IEvent {}

class AskLimitOrderEvent(
    override val id: String,
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val price: BigDecimal,
    val amount: BigDecimal
) : IEvent {}

class AskMarketOrderEvent(
    override val id: String,
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val amount: BigDecimal
) : IEvent {}

class BidLimitOrderEvent(
    override val id: String,
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val price: BigDecimal,
    val amount: BigDecimal
) : IEvent {}

class BidMarketOrderEvent(
    override val id: String,
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val amount: BigDecimal
) : IEvent {}

class CancelAskLimitOrderEvent(
    override val id: String,
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val price: BigDecimal,
    val amount: BigDecimal
) : IEvent {}

class CancelBidLimitOrderEvent(
    override val id: String,
    val userId: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val price: BigDecimal,
    val amount: BigDecimal
) : IEvent {}
