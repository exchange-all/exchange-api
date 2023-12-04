package com.exchange.orderbook.service

import com.exchange.orderbook.repository.disk.BalanceRepository
import com.exchange.orderbook.repository.memory.BalanceInMemoryRepository
import org.springframework.stereotype.Service

/**
 * @author thaivc
 * @since 2023
 */
@Service
class OrderBookDataManager(
    private val balanceMemoryRepository: BalanceInMemoryRepository,
    private val balanceRepository: BalanceRepository
) {
  fun loadData() {
    val balances = balanceRepository.findAll()
    balances.forEach { balanceMemoryRepository.upsert(it) }
  }
}
