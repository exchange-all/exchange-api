package com.exchange.orderbook.service

import com.exchange.orderbook.SpringContext
import com.exchange.orderbook.model.entity.BalanceEntity
import com.exchange.orderbook.model.entity.OffsetEntity
import com.exchange.orderbook.model.entity.OrderEntity
import com.exchange.orderbook.model.event.EventResponse
import com.exchange.orderbook.model.event.SuccessResponse
import com.exchange.orderbook.repository.disk.BalanceRepository
import com.exchange.orderbook.repository.disk.OffsetRepository
import com.exchange.orderbook.repository.disk.OrderRepository
import java.util.concurrent.Executors
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.Transactional

/**
 * @author thaivc
 * @since 2023
 */
@Configuration
class OutboundListener(
  private val outboundHandler: EventOutboundHandler,
  private val offsetRepository: OffsetRepository,
  private val balanceRepository: BalanceRepository,
  private val orderRepository: OrderRepository
) {
  @Value("\${order-book.topic}")
  private lateinit var orderBookTopic: String
  private val syncDatabaseExecutor = Executors.newSingleThreadExecutor()

  /**
   * Enqueue data to be processed asynchronously.
   *
   * @param offset
   * @param data
   */
  fun enqueue(offset: Long, data: List<EventResponse>) {
    syncDatabaseExecutor.execute { dequeue(offset, data) }
  }

  /**
   * Dequeue data to be processed synchronously. Persist data to database and publish to kafka.
   *
   * @param offset
   * @param data
   *
   * TODO: need to optimize this flow: persist in batch with latest value
   */
  fun dequeue(offset: Long, data: List<EventResponse>) {
    if (data.isEmpty()) return

    SpringContext.getBean(OutboundListener::class.java)
      .persist(offset, data.filterIsInstance<SuccessResponse>())

    response(data)
  }

  /**
   * Persist data to database. Including: offset, balance, order book.
   *
   * @param offset
   * @param data
   */
  @Transactional(rollbackFor = [Exception::class])
  fun persist(offset: Long, data: List<SuccessResponse>) {
    persistOffset(offset)
    persistBalance(data)
    persistOrder(data)
  }

  /**
   * Persist offset to database.
   *
   * @param offset
   */
  private fun persistOffset(offset: Long) {
    val offsetEntity =
      OffsetEntity().apply {
        this.id = "ORDER_BOOK"
        this.offset = offset
        this.partition = 0
        this.topic = orderBookTopic
      }
    offsetRepository.save(offsetEntity)
  }

  /**
   * Replace balances to database.
   *
   * @param data
   */
  private fun persistBalance(data: List<SuccessResponse>) {
    data
      .map { it.data }
      .filterIsInstance<BalanceEntity>().associateBy { it.id }
      .let { if (it.isNotEmpty()) balanceRepository.saveAll(it.values) }
  }

  /**
   * Replace orders to database.
   *
   * @param data
   */
  private fun persistOrder(data: List<SuccessResponse>) {
    data
      .map { it.data }
      .filterIsInstance<OrderEntity>().associateBy { it.id }
      .let { if (it.isNotEmpty()) orderRepository.saveAll(it.values) }
  }

  /**
   * Publish response to kafka.
   *
   * @param data
   */
  private fun response(data: List<EventResponse>) {
    outboundHandler.publishEvent(data)
  }
}
