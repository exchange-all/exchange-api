package com.exchange.orderbook.model.event

import com.exchange.orderbook.model.entity.BalanceEntity
import com.exchange.orderbook.model.entity.Cloneable
import com.exchange.orderbook.model.entity.OrderEntity
import java.math.BigDecimal

/**
 * @author thaivc
 * @since 2023
 */

interface SnapshotSupport {
}

open class EventResponse {
  var id: String? = null

  companion object {
    fun ok(event: IEvent, data: Cloneable): SuccessResponse {
      return SuccessResponse().apply {
        this.id = event.id
        this.event = event
        this.data = data
      }
    }

    fun fail(event: IEvent, error: String): FailResponse {
      return FailResponse().apply {
        this.id = event.id
        this.event = event
        this.error = error
      }
    }
  }
}

class SuccessResponse() : EventResponse(), SnapshotSupport {
  lateinit var event: IEvent
  lateinit var data: Cloneable
}

class FailResponse() : EventResponse() {
  lateinit var event: IEvent
  lateinit var error: String
}

class TradingResult : EventResponse(), SnapshotSupport {
  lateinit var remainOrder: OrderEntity
  lateinit var baseBalance: BalanceEntity
  lateinit var quoteBalance: BalanceEntity
  lateinit var tradedAmount: BigDecimal
  lateinit var tradedPrice: BigDecimal
}
