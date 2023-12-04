package com.exchange.orderbook.repository.memory

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * TODO: currently support only for Propagation.REQUIRES_NEW, need to support other propagation
 *
 * @author thaivc
 * @since 2023
 */
@Component
class MemoryTransactionManager(memoryRepository: Map<String, MemoryRepository>) {
  init {
    ROLLBACK_MAP =
        memoryRepository
            .map { (_, repository) -> repository::class.java.simpleName to repository::rollback }
            .toMap()
    log.info("Registered rollback map: {}", ROLLBACK_MAP)
  }

  companion object {
    private val log = LoggerFactory.getLogger(MemoryTransactionManager::class.java)

    private lateinit var ROLLBACK_MAP: Map<String, () -> Unit>

    val ENABLE_TRANSACTION: ThreadLocal<Boolean> = ThreadLocal.withInitial { null }
    val DATA_SEGMENT: ThreadLocal<Set<String>> = ThreadLocal.withInitial { null }

    /**
     * Wrap new transaction for memory repository with Propagation.REQUIRES_NEW
     *
     * @param supplier logic execution
     */
    fun <T> newTxn(supplier: () -> T): T {
      ENABLE_TRANSACTION.set(true)
      DATA_SEGMENT.get() ?: DATA_SEGMENT.set(mutableSetOf())
      try {
        return supplier()
      } catch (e: Exception) {
        log.error("Error occurred while executing transaction", e)
        rollback()
        throw e
      } finally {
        ENABLE_TRANSACTION.set(null)
        DATA_SEGMENT.set(null)
      }
    }

    private fun rollback() {
      DATA_SEGMENT.get()?.forEach { simpleClassName ->
        ROLLBACK_MAP[simpleClassName]?.run { rollback() }
      }
    }
  }
}
