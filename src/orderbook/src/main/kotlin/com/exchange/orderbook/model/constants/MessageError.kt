package com.exchange.orderbook.model.constants

/**
 * @author thaivc
 * @since 2023
 */
class MessageError {
    companion object {
        const val BALANCE_EXISTED = "BALANCE_EXISTED"
        const val BALANCE_NOT_FOUND = "BALANCE_NOT_FOUND"
        const val BASE_BALANCE_NOT_FOUND = "BASE_BALANCE_NOT_FOUND"
        const val QUOTE_BALANCE_NOT_FOUND = "QUOTE_BALANCE_NOT_FOUND"
        const val EVENT_NOT_FOUND = "EVENT_NOT_FOUND"
        const val BALANCE_NOT_ENOUGH = "BALANCE_NOT_ENOUGH"
        const val TRADING_PAIR_NOT_FOUND = "TRADING_PAIR_NOT_FOUND"
        const val ORDER_NOT_FOUND = "ORDER_NOT_FOUND"
    }
}
