package com.exchange.orderbook.model.event

/**
 * @author thaivc
 * @since 2023
 */
class UnknownEvent(override val id: String = "unknown") : IEvent {
    companion object {
        val DEFAULT = UnknownEvent()
    }
}
