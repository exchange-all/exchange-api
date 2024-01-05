package com.exchange.exchange.domain.market

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

/**
 * @author uuhnaut69
 *
 */
@Service
class MarketService(
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
    private val reactiveRedisOperations: ReactiveRedisOperations<String, String>
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MarketService::class.java)
    }

    /**
     * Subscribe market window trades.
     *
     * @param granularity the window size
     * @return the SSE flux
     */
    fun subscribeMarketWindowTrades(
        tradingPairId: String,
        granularity: WindowSize
    ): Flux<ServerSentEvent<WindowedTrade>> {
        return this.reactiveRedisOperations.listenToChannel(MarketDataConfig.windowSizeConfigTopicMap[granularity])
            .map { objectMapper.readValue(it.message, WindowedTrade::class.java) }
            .filter { it.tradingPairId == tradingPairId }
            .doOnNext { LOGGER.debug("Received message: {}", it) }
            .map {
                ServerSentEvent.builder<WindowedTrade>()
                    .event(MarketDataConfig.WINDOWED_TRADES_EVENT_TYPE)
                    .data(it)
                    .build()
            }
    }

    /**
     * Subscribe trade histories.
     *
     * @return the SSE flux
     */
    fun subscribeTradeHistories(
        tradingPairId: String,
    ): Flux<ServerSentEvent<TradingHistory>> {
        return this.reactiveRedisOperations.listenToChannel(MarketDataConfig.TRADE_HISTORIES_TOPIC)
            .map { objectMapper.readValue(it.message, TradingHistory::class.java) }
            .filter { it.tradingPairId == tradingPairId }
            .doOnNext { LOGGER.debug("Received message: {}", it) }
            .map {
                ServerSentEvent.builder<TradingHistory>()
                    .event(MarketDataConfig.TRADES_HISTORIES_EVENT_TYPE)
                    .data(it)
                    .build()
            }
    }
}
