package com.exchange.exchange.domain.balance

import com.exchange.exchange.core.Event

/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
data class BalanceCreated(
    override val aggregateId: String,
) : Event()

data class BalanceDeposited(
    override val aggregateId: String,
) : Event()

data class BalanceWithdrawn(
    override val aggregateId: String,
) : Event()
