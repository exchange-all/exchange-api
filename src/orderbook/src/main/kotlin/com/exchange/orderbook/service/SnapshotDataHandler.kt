package com.exchange.orderbook.service

import com.exchange.orderbook.SpringContext
import com.exchange.orderbook.model.entity.BalanceEntity
import com.exchange.orderbook.model.entity.OrderEntity
import com.exchange.orderbook.model.event.BalanceChangedEvent
import com.exchange.orderbook.model.event.SnapshotSupport
import com.exchange.orderbook.model.event.SuccessResponse
import com.exchange.orderbook.model.event.TradingResult
import com.exchange.orderbook.repository.disk.BalanceRepository
import com.exchange.orderbook.repository.disk.OffsetRepository
import com.exchange.orderbook.repository.disk.OrderRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
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
    private var balances: MutableMap<String, BalanceEntity> = HashMap()
    private var orders: MutableMap<String, OrderEntity> = HashMap()

    @Scheduled(fixedDelayString = "\${schedule.sync-database-interval}")
    fun scheduleSnapshot() {
        if (offset == null) return
        snapshotDataQueue.execute { SpringContext.getBean(SnapshotDataHandler::class.java).persist() }
    }

    fun enqueue(offset: Long, data: List<SnapshotSupport>) {
        snapshotDataQueue.execute { dequeue(offset, data) }
    }

    fun dequeue(offset: Long, data: List<SnapshotSupport>) {
        this.offset = offset + 1
        data.forEach {
            when (it) {
                is SuccessResponse -> {
                    when (val snapshotData = it.data) {
                        is BalanceEntity -> balances[snapshotData.id] = snapshotData
                        is OrderEntity -> orders[snapshotData.id] = snapshotData
                    }
                }

                is BalanceChangedEvent -> balances[it.balance.id] = it.balance

                is TradingResult -> {
                    balances[it.baseBalance.id] = it.baseBalance
                    balances[it.quoteBalance.id] = it.quoteBalance
                    orders[it.remainOrder.id] = it.remainOrder
                }
            }
        }
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
        balances = HashMap()
        orders = HashMap()
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
        if (balances.isEmpty()) return
        balanceRepository.saveAll(balances.values)
    }

    /**
     * Replace orders to database.
     */
    private fun persistOrder() {
        if (orders.isEmpty()) return
        orderRepository.saveAll(orders.values)
    }
}
