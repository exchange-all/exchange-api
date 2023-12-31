package com.exchange.orderbook.repository.memory

import com.exchange.orderbook.model.entity.Cloneable
import com.exchange.orderbook.model.entity.OrderEntity
import org.springframework.stereotype.Repository
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
@Repository
class OrderInMemoryRepository : MemoryRepositoryRollback<OrderEntity, String> {
    override lateinit var segments: ThreadLocal<MutableMap<String, Cloneable?>>
    override var data: MutableMap<String, OrderEntity> = HashMap()

    fun findById(id: String): OrderEntity? {
        return data[id]
    }

    fun upsert(item: OrderEntity) {
        data[item.id] = item
    }

    fun remove(item: OrderEntity) {
        data.remove(item.id)
    }
}
