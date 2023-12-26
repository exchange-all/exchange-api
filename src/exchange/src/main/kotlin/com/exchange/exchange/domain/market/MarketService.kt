package com.exchange.exchange.domain.market

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver

/**
 * @author uuhnaut69
 *
 */
@Service
class MarketService(
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
    private val kafkaReceiver: KafkaReceiver<String, String>,
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MarketService::class.java)
    }

    /**
     * Subscribe market window trades.
     *
     * @param windowSize the window size
     * @return the SSE flux
     */
    fun subscribeMarketWindowTrades(
        tradingPairId: String,
        windowSize: WindowSize
    ): Flux<ServerSentEvent<WindowedTrade>> {
        return this.kafkaReceiver.receive()
            .doOnNext { it.receiverOffset().acknowledge() }
            .doOnNext { LOGGER.debug("Received message: {}", it.value()) }
            .filter { it.topic() == MarketDataConfig.windowSizeConfigTopicMap[windowSize] }
            .map { objectMapper.readValue(it.value(), WindowedTrade::class.java) }
            .filter{ it.tradingPairId == tradingPairId }
            .map {
                ServerSentEvent.builder<WindowedTrade>()
                    .event(MarketDataConfig.WINDOWED_TRADES_EVENT_TYPE)
                    .data(it)
                    .build()
            }
    }

    /**
     * Subscribe trades histories.
     *
     * @return the SSE flux
     */
    fun subscribeTradesHistories(
        tradingPairId: String,
    ): Flux<ServerSentEvent<TradingHistory>> {
        return this.kafkaReceiver.receive()
            .doOnNext { it.receiverOffset().acknowledge() }
            .doOnNext { LOGGER.debug("Received message: {}", it.value()) }
            .filter { it.topic() == MarketDataConfig.TRADES_HISTORIES_TOPIC }
            .map { objectMapper.readValue(it.value(), TradingHistory::class.java) }
            .filter{ it.tradingPairId == tradingPairId }
            .map {
                ServerSentEvent.builder<TradingHistory>()
                    .event(MarketDataConfig.TRADES_HISTORIES_TOPIC)
                    .data(it)
                    .build()
            }
    }
}
