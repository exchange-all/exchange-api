package com.exchange.exchange.domain.balance

import com.exchange.exchange.core.Event

/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
data class DepositEvent(
    override val aggregateId: String,
) : Event()

data class WithdrawEvent(
    override val aggregateId: String,
) : Event()
