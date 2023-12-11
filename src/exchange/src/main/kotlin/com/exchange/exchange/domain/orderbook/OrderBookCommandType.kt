package com.exchange.exchange.domain.orderbook

import com.exchange.exchange.core.CommandType

/**
 * @author uuhnaut69
 *
 */
enum class OrderBookCommandType(
    override val type: String
) : CommandType {
    CREATE_ASK_LIMIT_ORDER("CREATE_ASK_LIMIT_ORDER"),
    CREATE_BID_LIMIT_ORDER("CREATE_BID_LIMIT_ORDER"),
    CANCEL_ORDER("CANCEL_ORDER"),
    ;
}
