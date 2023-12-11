package com.exchange.exchange.domain.orderbook

import com.exchange.exchange.core.EventType

/**
 * @author uuhnaut69
 *
 */
enum class OrderBookEventType(
    override val type: String
) : EventType {
    TRADING_RESULT("TRADING_RESULT"),
    CREATE_ASK_LIMIT_ORDER_SUCCESS("CREATE_ASK_LIMIT_ORDER_SUCCESS"),
    CREATE_ASK_LIMIT_ORDER_FAILED("CREATE_ASK_LIMIT_ORDER_FAILED"),
    CREATE_BID_LIMIT_ORDER_SUCCESS("CREATE_BID_LIMIT_ORDER_SUCCESS"),
    CREATE_BID_LIMIT_ORDER_FAILED("CREATE_BID_LIMIT_ORDER_FAILED"),
    CANCEL_ORDER_SUCCESS("CANCEL_ORDER_SUCCESS"),
    CANCEL_ORDER_FAILED("CANCEL_ORDER_FAILED"),
    ;
}
