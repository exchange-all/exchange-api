package com.exchange.exchange.domain.balance

import com.exchange.exchange.core.CommandType

/**
 *
 * @author uuhnaut69
 *
 */
enum class BalanceCommandType(
        override val type: String,
) : CommandType {
    // -- Create balance context
    CREATE_BALANCE("CREATE_BALANCE"),

    // -- Deposit balance context
    DEPOSIT_BALANCE("DEPOSIT_BALANCE"),

    // -- Withdraw balance context
    WITHDRAW_BALANCE("WITHDRAW_BALANCE")
    ;

}
