package com.exchange.orderbook.repository.memory

import org.springframework.transaction.annotation.Propagation

/**
 * @author thaivc
 * @since 2023
 */

class MemoryTransactional {
  var propagation: Propagation? = Propagation.REQUIRED
}
