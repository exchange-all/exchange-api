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

class UnknownFormatEvent(
    override val id: String = "unknown_format",
) : IEvent {
    var message: String = "Unknown format"

    companion object {
        val DEFAULT = UnknownFormatEvent()
        fun of(message: String?) = UnknownFormatEvent().apply {
            this.message = message ?: "Unknown format"
        }
    }
}
