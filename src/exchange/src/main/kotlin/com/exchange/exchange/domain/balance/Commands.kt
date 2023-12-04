package com.exchange.exchange.domain.balance

import com.exchange.exchange.core.Command
import java.math.BigDecimal

/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */

data class DepositCommand(
    val userId: String,
    val accountId: String,
    val amount: BigDecimal,
) : Command()

data class WithdrawCommand(
    val userId: String,
    val accountId: String,
    val amount: BigDecimal,
) : Command()
