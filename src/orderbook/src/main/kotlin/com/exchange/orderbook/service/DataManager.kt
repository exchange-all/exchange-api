package com.exchange.orderbook.service

import com.exchange.orderbook.repository.disk.BalanceRepository
import com.exchange.orderbook.repository.disk.OrderRepository
import com.exchange.orderbook.repository.disk.TradingPairRepository
import com.exchange.orderbook.repository.memory.BalanceInMemoryRepository
import com.exchange.orderbook.repository.memory.TradingPairInMemoryRepository
import org.springframework.stereotype.Service

/**
 * @author thaivc
 * @since 2023
 */
@Service
class DataManager(
    private val balanceMemoryRepository: BalanceInMemoryRepository,
    private val balanceRepository: BalanceRepository,
    private val matchingEngine: MatchingEngine,
    private val orderRepository: OrderRepository,
    private val tradingPairInMemoryRepository: TradingPairInMemoryRepository,
    private val tradingPairRepository: TradingPairRepository
) {
    fun restoreData() {
        restoreBalances()
        restoreTradingPairs()
        restoreOrders()
    }

    private fun restoreBalances() {
        val balances = balanceRepository.findAll()
        balances.forEach { balanceMemoryRepository.upsert(it) }
    }

    private fun restoreTradingPairs() {
        val tradingPairs = tradingPairRepository.findAll()
        tradingPairs.forEach {
            tradingPairInMemoryRepository.upsert(it)
        }
    }

    private fun restoreOrders() {
        val orders = orderRepository.findAll()
        matchingEngine.restoreData(orders)
    }
}
