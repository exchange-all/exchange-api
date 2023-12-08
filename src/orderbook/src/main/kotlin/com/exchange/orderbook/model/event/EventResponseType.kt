package com.exchange.orderbook.model.event

/**
 * @author cafe
 * @since 2023
 */
class EventResponseType {
    companion object {
        fun success(type: String): String {
            return "${type}_SUCCESS"
        }

        fun fail(type: String): String {
            return "${type}_FAILED"
        }

        const val TRADING_RESULT = "TRADING_RESULT"
    }
}