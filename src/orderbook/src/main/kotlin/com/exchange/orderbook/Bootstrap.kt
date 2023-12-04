package com.exchange.orderbook

import com.exchange.orderbook.service.EventInboundHandler
import com.exchange.orderbook.service.OrderBookDataManager
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * @author thaivc
 * @since 2023
 */
@Component
class Bootstrap(
    private val eventInboundHandler: EventInboundHandler,
    private val migrateData: MigrateData,
    private val orderBookDataManager: OrderBookDataManager
) {

  @EventListener(ApplicationReadyEvent::class)
  fun bootstrap() {
    migrateData.initData()
    orderBookDataManager.loadData()
    Thread(eventInboundHandler::pollingMessage).start()
  }
}
