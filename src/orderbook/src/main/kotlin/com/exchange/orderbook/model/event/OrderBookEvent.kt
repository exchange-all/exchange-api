package com.exchange.orderbook.model.event

import java.math.BigDecimal
import java.util.*

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
  val currency: String,
  val price: BigDecimal,
  val amount: BigDecimal
) : IEvent {}

class AskMarketOrderEvent(
  override val id: String,
  val userId: String,
  val currency: String,
  val amount: BigDecimal
) : IEvent {}

class BidLimitOrderEvent(
  override val id: String,
  val userId: String,
  val currency: String,
  val price: BigDecimal,
  val amount: BigDecimal
) : IEvent {}

class BidMarketOrderEvent(
  override val id: String,
  val userId: String,
  val currency: String,
  val amount: BigDecimal
) : IEvent {}
