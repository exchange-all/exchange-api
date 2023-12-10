package com.exchange.orderbook.model.constants

/**
 * @author thaivc
 * @since 2023
 */
class HeaderType {
    companion object {
        const val CE_TYPE = "ce_type"
        const val REPLY_TOPIC = "kafka_replyTopic"
        const val CORRELATION_ID = "kafka_correlationId"
    }
}
