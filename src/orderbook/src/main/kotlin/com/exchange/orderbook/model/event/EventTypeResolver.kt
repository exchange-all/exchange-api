package com.exchange.orderbook.model.event

/**
 * @author thaivc
 * @since 2023
 */

val eventTypeMap: Map<String, Class<out IEvent>> = mapOf(
    IEvent.CREATE_BALANCE to CreateBalanceEvent::class.java,
    IEvent.DEPOSIT_BALANCE to DepositBalanceEvent::class.java,
    IEvent.WITHDRAW_BALANCE to WithdrawBalanceEvent::class.java,
    IEvent.ORDER_BOOK_ASK_BY_LIMIT to AskLimitOrderEvent::class.java,
    IEvent.ORDER_BOOK_ASK_BY_MARKET to AskMarketOrderEvent::class.java,
    IEvent.ORDER_BOOK_BID_BY_LIMIT to BidLimitOrderEvent::class.java,
    IEvent.ORDER_BOOK_BID_BY_MARKET to BidMarketOrderEvent::class.java
)

fun typeOf(name: String): Class<out IEvent>? {
    return eventTypeMap[name]
}
