package com.exchange.orderbook.repository.memory

import com.exchange.orderbook.model.entity.BalanceEntity
import com.exchange.orderbook.model.entity.Cloneable
import org.springframework.stereotype.Repository
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
@Repository
class BalanceInMemoryRepository : MemoryRepositoryRollback<BalanceEntity, UUID> {
    override val data: MutableMap<UUID, BalanceEntity> = HashMap()
    override val segments: ThreadLocal<MutableMap<UUID, Cloneable?>> =
        ThreadLocal.withInitial { null }

    // user_id -> Set<balance_id>
    private val userIdIndex: MutableMap<UUID, Set<UUID>> = HashMap()

    // user_id + currency -> balance_id
    private val userIdCurrencyIndex: MutableMap<Pair<UUID, String>, UUID> = HashMap()

    fun upsert(balance: BalanceEntity) {
        prepareSegment(balance)
        data[balance.id] = balance
        userIdIndex[balance.userId] =
            userIdIndex[balance.userId]?.plus(balance.id) ?: mutableSetOf(balance.id)
        userIdCurrencyIndex[Pair(balance.userId, balance.currency)] = balance.id
    }

    fun findById(id: UUID): BalanceEntity? {
        return data[id]
    }

    fun findByUserId(userId: UUID): List<BalanceEntity>? {
        val ids = userIdIndex[userId]
        return ids?.mapNotNull { data[it] } ?: emptyList()
    }

    fun findByUserIdAndCurrency(userId: UUID, currency: String): BalanceEntity? {
        val id = userIdCurrencyIndex[Pair(userId, currency)]
        return data[id]
    }
}
