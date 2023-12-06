package com.exchange.orderbook.service

import com.exchange.orderbook.model.event.EventResponse
import com.exchange.orderbook.model.event.SuccessResponse
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
  fun enqueue(offset: Long, data: List<EventResponse>) {
    outboundListenerQueue.execute { dequeue(offset, data) }
  }

  /**
   * Dequeue data to be processed synchronously. Persist data to database and publish to kafka.
   *
   * @param offset
   * @param data
   *
   */
  fun dequeue(offset: Long, data: List<EventResponse>) {
    if (data.isEmpty()) return

    snapshotDataHandler.enqueue(offset, data.filterIsInstance<SuccessResponse>())
    outboundHandler.publishEvent(data)
  }
}
