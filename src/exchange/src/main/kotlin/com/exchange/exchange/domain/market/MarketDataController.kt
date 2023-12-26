package com.exchange.exchange.domain.market

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
        @RequestParam(required = true) size: WindowSize
    ): Flux<ServerSentEvent<WindowedTrade>> {
        return this.marketService.subscribeMarketWindowTrades(tradingPairId, size)
    }

    @GetMapping("/{tradingPairId}/trades-histories")
    fun subscribeTrading(
        @PathVariable tradingPairId: String,
    ): Flux<ServerSentEvent<TradingHistory>> {
        return this.marketService.subscribeTradesHistories(tradingPairId)
    }
}
