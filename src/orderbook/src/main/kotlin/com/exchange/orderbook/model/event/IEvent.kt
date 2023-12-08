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
        const val ORDER_BOOK_ASK_BY_LIMIT = "ORDER_BOOK_ASK_BY_LIMIT"
        const val ORDER_BOOK_ASK_BY_MARKET = "ORDER_BOOK_ASK_BY_MARKET"
        const val ORDER_BOOK_BID_BY_LIMIT = "ORDER_BOOK_BID_BY_LIMIT"
        const val ORDER_BOOK_BID_BY_MARKET = "ORDER_BOOK_BID_BY_MARKET"
    }
}
