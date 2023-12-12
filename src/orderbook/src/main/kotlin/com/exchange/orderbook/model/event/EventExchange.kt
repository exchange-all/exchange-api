package com.exchange.orderbook.model.event

import com.exchange.orderbook.model.entity.BalanceEntity
import com.exchange.orderbook.model.entity.Cloneable
import com.exchange.orderbook.model.entity.OrderEntity
import java.math.BigDecimal

/**
 * @author thaivc
 * @since 2023
 */

interface ExchangeEvent {}
interface SnapshotSupport {}

open class EventResponse: ExchangeEvent {
    var id: String? = null

    companion object {
        fun ok(event: IEvent, data: Cloneable): SuccessResponse {
            return SuccessResponse(event, data).apply {
                this.id = event.id
            }
        }

        fun fail(event: IEvent, error: String): FailResponse {
            return FailResponse(event, error).apply {
                this.id = event.id
            }
        }
    }
}

data class SuccessResponse(
    var event: IEvent,
    var data: Cloneable
) : EventResponse() {
}

data class FailResponse(
    var event: IEvent,
    var error: String
) : EventResponse() {}

interface NotResponse {}

data class BalanceChangedEvent(
    val balance: BalanceEntity
) : SnapshotSupport, ExchangeEvent, NotResponse {}

data class OrderChangedEvent(
    val order: OrderEntity
) : SnapshotSupport, ExchangeEvent, NotResponse {}

data class TradingResult(
    val remainOrder: OrderEntity,
    val baseBalance: BalanceEntity,
    val quoteBalance: BalanceEntity,
    val tradedAmount: BigDecimal,
    val tradedPrice: BigDecimal
) : ExchangeEvent {}
