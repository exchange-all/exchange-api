package com.exchange.orderbook.repository.memory

import com.exchange.orderbook.model.entity.BalanceEntity
import java.util.*
import org.springframework.stereotype.Repository

/**
 * @author thaivc
 * @since 2023
 */
@Repository
class BalanceInMemoryRepository : MemoryRepositoryRollback<BalanceEntity, UUID> {
  override val data: MutableMap<UUID, BalanceEntity> = HashMap()
  override val segments: ThreadLocal<MutableMap<UUID, BalanceEntity?>> =
      ThreadLocal.withInitial { null }

  // user_id -> balance_id
  private val userIdIndex: MutableMap<UUID, UUID> = HashMap()

  // user_id + currency -> balance_id
  private val userIdCurrencyIndex: MutableMap<Pair<UUID, String>, UUID> = HashMap()

  fun upsert(balance: BalanceEntity) {
    prepareSegment(balance)
    data[balance.id] = balance
    userIdIndex[balance.userId] = balance.id
    userIdCurrencyIndex[Pair(balance.userId, balance.currency)] = balance.id
  }

  fun findById(id: UUID): BalanceEntity? {
    return data[id]
  }

  fun findByUserId(userId: UUID): BalanceEntity? {
    val id = userIdIndex[userId]
    return data[id]
  }

  fun findByUserIdAndCurrency(userId: UUID, currency: String): BalanceEntity? {
    val id = userIdCurrencyIndex[Pair(userId, currency)]
    return data[id]
  }
}
