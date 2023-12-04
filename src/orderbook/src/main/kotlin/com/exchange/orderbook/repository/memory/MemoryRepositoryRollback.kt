package com.exchange.orderbook.repository.memory

import com.exchange.orderbook.model.entity.Identifiable
import java.io.Serializable

/**
 * @author thaivc
 * @since 2023
 */
interface MemoryRepositoryRollback<ENTITY: Identifiable<ID>, ID: Serializable>: MemoryRepository {
  val segments: ThreadLocal<MutableMap<ID, ENTITY?>>
  val data: MutableMap<ID, ENTITY>

  fun prepareSegment(entity: ENTITY) {
    if (MemoryTransactionManager.ENABLE_TRANSACTION.get() == true) {
      MemoryTransactionManager.DATA_SEGMENT.get().plus(entity::class.simpleName)
      if (segments.get() == null) {
        segments.set(mutableMapOf())
      }
      segments.get().putIfAbsent(entity.id, data[entity.id])
    }
  }

  override fun rollback() {
    segments.get().forEach() { (k, v) ->
      if (v == null) {
        // case: rollback insert
        data.remove(k)
      } else {
        data[k] = v
      }
    }
  }
}
