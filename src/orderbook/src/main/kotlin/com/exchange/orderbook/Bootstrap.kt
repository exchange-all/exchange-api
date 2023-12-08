package com.exchange.orderbook

import com.exchange.orderbook.service.DataManager
import com.exchange.orderbook.service.EventInboundHandler
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
    private val dataManager: DataManager
) {

    @EventListener(ApplicationReadyEvent::class)
    fun bootstrap() {
        migrateData.initData()
        dataManager.restoreData()
        Thread(eventInboundHandler::pollingMessage).start()
    }
}
