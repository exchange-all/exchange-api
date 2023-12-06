package com.exchange.orderbook.service

import com.exchange.orderbook.SpringContext
import com.exchange.orderbook.model.entity.BalanceEntity
import com.exchange.orderbook.model.entity.OrderEntity
import com.exchange.orderbook.model.event.SuccessResponse
import com.exchange.orderbook.repository.disk.BalanceRepository
import com.exchange.orderbook.repository.disk.OffsetRepository
import com.exchange.orderbook.repository.disk.OrderRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.Executors

/**
 * @author thaivc
 * @since 2023
 */
@Component
class SnapshotDataHandler(
  private val offsetRepository: OffsetRepository,
  private val balanceRepository: BalanceRepository,
  private val orderRepository: OrderRepository
) {

  private val snapshotDataQueue = Executors.newSingleThreadExecutor()

  private var offset: Long? = null
  private var balance: Map<UUID, BalanceEntity> = HashMap()
  private var order: Map<UUID, OrderEntity> = HashMap()

  @Scheduled(fixedDelayString = "\${schedule.sync-database-interval}")
  fun scheduleSnapshot() {
    if (offset == null) return
    snapshotDataQueue.execute { SpringContext.getBean(SnapshotDataHandler::class.java).persist() }
  }

  fun enqueue(offset: Long, data: List<SuccessResponse>) {
    snapshotDataQueue.execute { dequeue(offset, data) }
  }

  fun dequeue(offset: Long, data: List<SuccessResponse>) {
    this.offset = offset + 1
    balance = balance.plus(data.filter { it.data is BalanceEntity }.map { it.data as BalanceEntity }
      .associateBy { it.id })
    order.plus(data.filter { it.data is OrderEntity }.map { it.data as OrderEntity }
      .associateBy { it.id })
  }

  /**
   * Persist data to database. Including: offset, balance, order book.
   */
  @Transactional(rollbackFor = [Exception::class])
  fun persist() {
    if (offset == null) return
    persistOffset()
    persistBalance()
    persistOrder()
    reset()
  }

  fun reset() {
    offset = null
    balance = HashMap()
    order = HashMap()
  }

  /**
   * Persist offset to database.
   */
  private fun persistOffset() {
    offsetRepository.getOrderBookOffset()
      ?.let {
        it.offset = offset!!
        offsetRepository.save(it)
      }
  }

  /**
   * Replace balances to database.
   */
  private fun persistBalance() {
    if (balance.isEmpty()) return
    balanceRepository.saveAll(balance.values)
  }

  /**
   * Replace orders to database.
   */
  private fun persistOrder() {
    if (order.isEmpty()) return
    orderRepository.saveAll(order.values)
  }
}
