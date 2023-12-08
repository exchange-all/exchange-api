package com.exchange.exchange.domain.balance

import com.exchange.exchange.core.EventType

/**
 *
 * @author uuhnaut69
 *
 */
enum class BalanceEventType(
    override val type: String,
) : EventType {
    //-- Create balance context
    CREATE_BALANCE_SUCCESS("CREATE_BALANCE_SUCCESS"),
    CREATE_BALANCE_FAIL("CREATE_BALANCE_FAIL"),

    //-- Deposit balance context
    DEPOSIT_BALANCE_SUCCESS("DEPOSIT_BALANCE_SUCCESS"),
    DEPOSIT_BALANCE_FAIL("DEPOSIT_BALANCE_FAIL"),

    //-- Withdraw balance context
    WITHDRAW_BALANCE_SUCCESS("WITHDRAW_BALANCE_SUCCESS"),
    WITHDRAW_BALANCE_FAIL("WITHDRAW_BALANCE_FAIL")

    ;

}
