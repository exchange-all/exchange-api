package com.exchange.exchange.domain.balance

import com.exchange.exchange.core.Event

/**
 *
 * @author uuhnaut69
 *
 */
data class BalanceCreated(
    val balanceId: String,
) : Event()

data class BalanceDeposited(
    val balanceId: String,
) : Event()

data class BalanceWithdrawn(
    val balanceId: String,
) : Event()
