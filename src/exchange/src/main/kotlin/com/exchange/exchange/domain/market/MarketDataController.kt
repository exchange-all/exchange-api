package com.exchange.exchange.domain.market

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

/**
 * @author thaivc
 * @since 2023
 */
@Tag(name = "Market Data", description = "Market Data API")
@RestController
@RequestMapping("/api/v1/market-data")
class MarketDataController(
    private val marketService: MarketService
) {
    @GetMapping("/{tradingPairId}/windowed-trades")
    fun subscribeMarketWindowTrades(
        @PathVariable tradingPairId: String,
        @RequestParam(required = true) granularity: WindowSize
    ): Flux<ServerSentEvent<WindowedTrade>> {
        return this.marketService.subscribeMarketWindowTrades(tradingPairId, granularity)
    }

    @GetMapping("/{tradingPairId}/trade-histories")
    fun subscribeTrading(
        @PathVariable tradingPairId: String,
    ): Flux<ServerSentEvent<TradingHistory>> {
        return this.marketService.subscribeTradeHistories(tradingPairId)
    }
}
