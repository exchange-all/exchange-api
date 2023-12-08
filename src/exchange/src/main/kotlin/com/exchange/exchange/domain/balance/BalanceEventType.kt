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
    CREATE_BALANCE_FAILED("CREATE_BALANCE_FAILED"),

    //-- Deposit balance context
    DEPOSIT_BALANCE_SUCCESS("DEPOSIT_BALANCE_SUCCESS"),
    DEPOSIT_BALANCE_FAILED("DEPOSIT_BALANCE_FAILED"),

    //-- Withdraw balance context
    WITHDRAW_BALANCE_SUCCESS("WITHDRAW_BALANCE_SUCCESS"),
    WITHDRAW_BALANCE_FAILED("WITHDRAW_BALANCE_FAILED")

    ;

}
