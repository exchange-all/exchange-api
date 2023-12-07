package com.exchange.orderbook.model.event

/**
 * @author thaivc
 * @since 2023
 */
interface IEvent {
  val id: String

  companion object {
    const val BALANCE_CREATED = "BALANCE_CREATED"
    const val BALANCE_DEPOSITED = "BALANCE_DEPOSITED"
    const val BALANCE_WITHDRAWN = "BALANCE_WITHDRAWN"
    const val ORDER_BOOK_ASK_BY_LIMIT = "ORDER_BOOK_ASK_BY_LIMIT"
    const val ORDER_BOOK_ASK_BY_MARKET = "ORDER_BOOK_ASK_BY_MARKET"
    const val ORDER_BOOK_BID_BY_LIMIT = "ORDER_BOOK_BID_BY_LIMIT"
    const val ORDER_BOOK_BID_BY_MARKET = "ORDER_BOOK_BID_BY_MARKET"
  }
}
