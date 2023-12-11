package com.exchange.orderbook.model.event

/**
 * @author thaivc
 * @since 2023
 */

val eventTypeMap: Map<String, Class<out IEvent>> = mapOf(
    IEvent.CREATE_BALANCE to CreateBalanceEvent::class.java,
    IEvent.DEPOSIT_BALANCE to DepositBalanceEvent::class.java,
    IEvent.WITHDRAW_BALANCE to WithdrawBalanceEvent::class.java,
    IEvent.CREATE_ASK_LIMIT_ORDER to AskLimitOrderEvent::class.java,
    IEvent.CREATE_BID_LIMIT_ORDER to BidLimitOrderEvent::class.java,
    IEvent.CREATE_ASK_MARKET_ORDER to AskMarketOrderEvent::class.java,
    IEvent.CREATE_BID_MARKET_ORDER to BidMarketOrderEvent::class.java,
    IEvent.CANCEL_ORDER to CancelOrderEvent::class.java,
)

fun typeOf(name: String): Class<out IEvent>? {
    return eventTypeMap[name]
}
