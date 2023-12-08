package com.exchange.exchange.domain.balance

import com.exchange.exchange.core.Command
import java.math.BigDecimal

/**
 *
 * @author uuhnaut69
 *
 */
data class CreateBalanceCommand(
    val id: String,
    val userId: String,
    val currency: String,
) : Command()

data class DepositCommand(
    val id: String,
    val userId: String,
    val currency: String,
    val amount: BigDecimal,
) : Command()

data class WithdrawCommand(
    val id: String,
    val userId: String,
    val currency: String,
    val amount: BigDecimal,
) : Command()
