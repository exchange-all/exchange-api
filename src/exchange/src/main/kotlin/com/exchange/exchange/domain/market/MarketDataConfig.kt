package com.exchange.exchange.domain.market

/**
 * @author uuhnaut69
 *
 */
object MarketDataConfig {

    const val WINDOWED_TRADES_EVENT_TYPE = "WINDOWED-TRADES"
    const val TRADES_HISTORIES_EVENT_TYPE = "TRADES-HISTORIES"

    const val TRADES_HISTORIES_TOPIC = "market-data.trades-histories"

    val windowSizeConfigTopicMap = mapOf(
        WindowSize.ONE_SECOND to "market-data.windowed-trades-1s",
        WindowSize.ONE_MINUTE to "market-data.windowed-trades-1m",
        WindowSize.THREE_MINUTE to "market-data.windowed-trades-3m",
        WindowSize.FIVE_MINUTE to "market-data.windowed-trades-5m",
        WindowSize.FIFTEEN_MINUTE to "market-data.windowed-trades-15m",
        WindowSize.THIRTY_MINUTE to "market-data.windowed-trades-30m",
        WindowSize.ONE_HOUR to "market-data.windowed-trades-1h",
        WindowSize.TWO_HOUR to "market-data.windowed-trades-2h",
        WindowSize.FOUR_HOUR to "market-data.windowed-trades-4h",
        WindowSize.SIX_HOUR to "market-data.windowed-trades-6h",
        WindowSize.EIGHT_HOUR to "market-data.windowed-trades-8h",
        WindowSize.TWELVE_HOUR to "market-data.windowed-trades-12h",
        WindowSize.ONE_DAY to "market-data.windowed-trades-1d",
        WindowSize.THREE_DAY to "market-data.windowed-trades-3d",
        WindowSize.ONE_WEEK to "market-data.windowed-trades-1w",
        WindowSize.ONE_MONTH to "market-data.windowed-trades-1M"
    )
}
