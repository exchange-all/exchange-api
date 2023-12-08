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
class OrderInMemoryRepository : MemoryRepositoryRollback<OrderEntity, UUID> {
    override lateinit var segments: ThreadLocal<MutableMap<UUID, Cloneable?>>
    override lateinit var data: MutableMap<UUID, OrderEntity>

    fun findById(id: UUID): OrderEntity? {
        return data[id]
    }

    fun upsert(item: OrderEntity) {
        data[item.id] = item
    }
}
