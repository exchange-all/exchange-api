package com.exchange.orderbook.repository.memory

import com.exchange.orderbook.model.entity.BalanceEntity
import com.exchange.orderbook.model.entity.Cloneable
import org.springframework.stereotype.Repository

/**
 * @author thaivc
 * @since 2023
 */
@Repository
class BalanceInMemoryRepository : MemoryRepositoryRollback<BalanceEntity, String> {
    override val data: MutableMap<String, BalanceEntity> = HashMap()
    override val segments: ThreadLocal<MutableMap<String, Cloneable?>> =
        ThreadLocal.withInitial { null }

    // user_id -> Set<balance_id>
    private val userIdIndex: MutableMap<String, Set<String>> = HashMap()

    // user_id + currency -> balance_id
    private val userIdCurrencyIndex: MutableMap<Pair<String, String>, String> = HashMap()

    fun upsert(balance: BalanceEntity) {
        prepareSegment(balance)
        data[balance.id] = balance
        userIdIndex[balance.userId] =
            userIdIndex[balance.userId]?.plus(balance.id) ?: mutableSetOf(balance.id)
        userIdCurrencyIndex[Pair(balance.userId, balance.currency)] = balance.id
    }

    fun findById(id: String): BalanceEntity? {
        return data[id]
    }

    fun findByUserId(userId: String): List<BalanceEntity>? {
        val ids = userIdIndex[userId]
        return ids?.mapNotNull { data[it] } ?: emptyList()
    }

    fun findByUserIdAndCurrency(userId: String, currency: String): BalanceEntity? {
        val id = userIdCurrencyIndex[Pair(userId, currency)]
        return data[id]
    }
}
