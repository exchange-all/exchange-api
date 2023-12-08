package com.exchange.exchange.domain.balance

import com.exchange.exchange.core.Event

/**
 *
 * @author uuhnaut69
 *
 */
data class BalanceCreated(
    val id: String,
) : Event()

data class BalanceDeposited(
    val id: String,
) : Event()

data class BalanceWithdrawn(
    val id: String,
) : Event()
