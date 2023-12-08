package com.exchange.orderbook.service

import com.exchange.orderbook.model.Tuple
import com.exchange.orderbook.model.event.EventResponse
import com.exchange.orderbook.model.event.SnapshotSupport
import org.apache.kafka.common.header.Headers
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

/**
 * @author thaivc
 * @since 2023
 */
@Component
class OutboundListener(
    private val outboundHandler: EventOutboundHandler,
    private val snapshotDataHandler: SnapshotDataHandler
) {

    private val outboundListenerQueue = Executors.newSingleThreadExecutor()

    /**
     * Enqueue data to be processed asynchronously.
     *
     * @param offset
     * @param data
     */
    fun enqueue(offset: Long, data: List<Tuple<EventResponse, Headers>>) {
        outboundListenerQueue.execute { dequeue(offset, data) }
    }

    /**
     * Dequeue data to be processed synchronously. Persist data to database and publish to kafka.
     *
     * @param offset
     * @param data
     *
     */
    fun dequeue(offset: Long, data: List<Tuple<EventResponse, Headers>>) {
        if (data.isEmpty()) return

        snapshotDataHandler.enqueue(offset, data.map { it.first }.filterIsInstance<SnapshotSupport>())
        outboundHandler.publishEvent(data)
    }
}
