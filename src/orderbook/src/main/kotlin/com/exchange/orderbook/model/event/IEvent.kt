package com.exchange.orderbook.model.event

/**
 * @author thaivc
 * @since 2023
 */
interface IEvent {
    val id: String

    companion object {
        const val CREATE_BALANCE = "CREATE_BALANCE"
        const val DEPOSIT_BALANCE = "DEPOSIT_BALANCE"
        const val WITHDRAW_BALANCE = "WITHDRAW_BALANCE"
        const val CREATE_ASK_LIMIT_ORDER = "CREATE_ASK_LIMIT_ORDER"
        const val CREATE_BID_LIMIT_ORDER = "CREATE_BID_LIMIT_ORDER"
        const val CREATE_ASK_MARKET_ORDER = "CREATE_ASK_MARKET_ORDER"
        const val CREATE_BID_MARKET_ORDER = "CREATE_BID_MARKET_ORDER"
    }
}
