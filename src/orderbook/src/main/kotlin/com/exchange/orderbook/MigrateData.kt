package com.exchange.orderbook

import com.exchange.orderbook.model.constants.Currency
import com.exchange.orderbook.model.entity.OffsetEntity
import com.exchange.orderbook.model.entity.TradingPairEntity
import com.exchange.orderbook.repository.disk.OffsetRepository
import com.exchange.orderbook.repository.disk.TradingPairRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
@Component
class MigrateData(
    private val offsetRepository: OffsetRepository,
    private val tradingPairRepository: TradingPairRepository
) {
    @Value("\${order-book.topic}")
    private lateinit var orderBookTopic: String

    companion object {
        private val log = LoggerFactory.getLogger(MigrateData::class.java)
    }

    fun initData() {
        checkAndMigrateOffset()
        checkAndMigrateTradingPair()
    }

    fun checkAndMigrateOffset() {
        val offsetEntity = offsetRepository.getOrderBookOffset()
        if (offsetEntity == null) {
            val newOffsetEntity =
                OffsetEntity().apply {
                    id = "ORDER_BOOK"
                    partition = 0
                    topic = orderBookTopic
                    offset = 0
                }
            offsetRepository.save(newOffsetEntity)
            log.info("INIT OFFSET ENTITY: $newOffsetEntity")
        }
    }

    fun checkAndMigrateTradingPair() {
        checkAndInitPair(Currency.BTC, Currency.USDT, 0.000_023)
        checkAndInitPair(Currency.USDT, Currency.BTC, 0.1)
    }

    private fun checkAndInitPair(base: Currency, quote: Currency, limit: Double) {
        val pair = tradingPairRepository.findFirstByBaseCurrencyAndQuoteCurrency(
            base.name,
            quote.name
        )
        if (pair == null) {
            tradingPairRepository.save(TradingPairEntity().apply {
                id = UUID.randomUUID().toString()
                baseCurrency = base.name
                quoteCurrency = quote.name
                minLimit = BigDecimal(limit)
            })
        }
    }
}
