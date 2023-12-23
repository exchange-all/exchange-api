package com.exchange.orderbook.service

import com.exchange.orderbook.model.TradingPair
import com.exchange.orderbook.model.currencyPair
import com.exchange.orderbook.model.entity.OrderEntity
import com.exchange.orderbook.model.entity.Status
import com.exchange.orderbook.model.entity.TradingPairEntity
import com.exchange.orderbook.model.event.BalanceChangedEvent
import com.exchange.orderbook.model.event.OrderChangedEvent
import com.exchange.orderbook.model.event.TradingResult
import com.exchange.orderbook.repository.memory.BalanceInMemoryRepository
import com.exchange.orderbook.repository.memory.OrderInMemoryRepository
import com.exchange.orderbook.repository.memory.TradingPairInMemoryRepository
import org.springframework.stereotype.Component
import java.math.RoundingMode
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
@Component
class MatchingEngine(
    private val balanceInMemoryRepository: BalanceInMemoryRepository,
    private val orderInMemoryRepository: OrderInMemoryRepository,
    private val tradingPairInMemoryRepository: TradingPairInMemoryRepository
) {

    /**
     * currencyPair -> TradingPair
     */
    val tradingPairs: MutableMap<String, TradingPair> = HashMap()

    /**
     * Load orders to memory
     */
    fun restoreData(orders: List<OrderEntity>) {
        orders.filter { it.status == Status.OPEN }.forEach {
            val pair = tradingPairInMemoryRepository.findById(it.tradingPairId)
            if (pair != null) {
                tradingPairs.computeIfAbsent(currencyPair(pair.baseCurrency, pair.quoteCurrency)) {
                    TradingPair(pair.baseCurrency, pair.quoteCurrency)
                }.addOrder(it)
            }
        }
    }

    /**
     * Add order
     *
     * @param order
     */
    fun addOrder(order: OrderEntity) {
        tradingPairInMemoryRepository.findById(order.tradingPairId)!!.let { tradingPair ->
            orderInMemoryRepository.upsert(order)
            tradingPairs.computeIfAbsent(currencyPair(tradingPair.baseCurrency, tradingPair.quoteCurrency)) {
                TradingPair(tradingPair.baseCurrency, tradingPair.quoteCurrency)
            }.addOrder(order)
        }
    }

    /**
     * Remove order from in-memory repository and matching engine
     *
     * @param order
     */
    fun removeOrder(order: OrderEntity) {
        tradingPairInMemoryRepository.findById(order.tradingPairId)!!.let { tradingPair ->
            orderInMemoryRepository.remove(order)
            tradingPairs[currencyPair(tradingPair.baseCurrency, tradingPair.quoteCurrency)]?.removeOrder(order)
        }
    }

    /**
     * Matching orders for currency pair
     *
     * @return list of trading results
     * @see MatchingEngineTest `test matching`
     */
    fun matching(baseCurrency: String, quoteCurrency: String): List<TradingResult> {
        val tradingPair = tradingPairs[currencyPair(baseCurrency, quoteCurrency)] ?: return emptyList()
        val currencyPair = tradingPairInMemoryRepository.findByCurrencyPair(baseCurrency, quoteCurrency)!!
        val tradingResults: MutableList<TradingResult> = mutableListOf()

        ASK_BID_PRICE_COMPARE@
        while (true) {
            if (tradingPair.asks.isEmpty() || tradingPair.bids.isEmpty()) break@ASK_BID_PRICE_COMPARE

            val askMinPrice = tradingPair.asks.firstKey()
            val bidMaxPrice = tradingPair.bids.firstKey()
            if (askMinPrice > bidMaxPrice) break@ASK_BID_PRICE_COMPARE

            val askOrders = tradingPair.asks[askMinPrice]!!
            val bidOrders = tradingPair.bids[bidMaxPrice]!!

            ASK_BID_AMOUNT_COMPARE@
            while (true) {
                if (askOrders.isEmpty() || bidOrders.isEmpty())
                    break@ASK_BID_AMOUNT_COMPARE

                val askOrderHead: OrderEntity = askOrders.entries.first().value
                val bidOrderHead: OrderEntity = bidOrders.entries.first().value
                this.updateBalances(askOrderHead, bidOrderHead, tradingPair, currencyPair ,tradingResults)
            }
        }
        return tradingResults
    }

    private fun updateBalances(
        askOrderHead: OrderEntity,
        bidOrderHead: OrderEntity,
        tradingPair: TradingPair,
        currencyPair: TradingPairEntity,
        tradingResults: MutableList<TradingResult>
    ) {
        // determine traded amount

        val tradedQuoteAmount = minOf(
            askOrderHead.availableAmount.multiply(askOrderHead.price),
            bidOrderHead.availableAmount.multiply(bidOrderHead.price)
        )
        val tradedAskBaseAmount = tradedQuoteAmount.divide(askOrderHead.price, RoundingMode.HALF_UP)
        val tradedBidBaseAmount = tradedQuoteAmount.divide(bidOrderHead.price, RoundingMode.HALF_UP)

        // add quote-currency availableAmount and subtract base-currency lockedAmount for ask
        val askQuoteBalance = balanceInMemoryRepository.findByUserIdAndCurrency(
            askOrderHead.userId,
            tradingPair.quoteCurrency
        )!!
        val askBaseBalance = balanceInMemoryRepository.findByUserIdAndCurrency(
            askOrderHead.userId,
            tradingPair.baseCurrency
        )!!
        askQuoteBalance.availableAmount =
            askQuoteBalance.availableAmount.add(tradedQuoteAmount)
        askBaseBalance.lockAmount = askBaseBalance.lockAmount.subtract(tradedAskBaseAmount)

        // add base-currency availableAmount and subtract quote-currency lockedAmount for ask
        val bidBaseBalance = balanceInMemoryRepository.findByUserIdAndCurrency(
            bidOrderHead.userId,
            tradingPair.baseCurrency
        )!!
        val bidQuoteBalance = balanceInMemoryRepository.findByUserIdAndCurrency(
            bidOrderHead.userId,
            tradingPair.quoteCurrency
        )!!
        bidBaseBalance.availableAmount = bidBaseBalance.availableAmount.add(tradedBidBaseAmount)
        bidQuoteBalance.lockAmount =
            bidQuoteBalance.lockAmount.subtract(tradedQuoteAmount)

        askOrderHead.availableAmount = askOrderHead.availableAmount.subtract(tradedAskBaseAmount)
        bidOrderHead.availableAmount = bidOrderHead.availableAmount.subtract(tradedBidBaseAmount)

        if (askOrderHead.availableAmount <= currencyPair.minLimit) {
            askOrderHead.status = Status.CLOSED
            tradingPair.removeOrder(askOrderHead)
        }
        if (bidOrderHead.availableAmount <= currencyPair.minLimit) {
            askOrderHead.status = Status.CLOSED
            tradingPair.removeOrder(bidOrderHead)
        }

        // add result
        val askResult = TradingResult(
            askOrderHead.clone(),
            askBaseBalance.clone(),
            askQuoteBalance.clone(),
            tradedAskBaseAmount,
            askOrderHead.price
        ).apply { askOrderHead.id }
        tradingResults.add(askResult)

        val bidResult = TradingResult(
            bidOrderHead.clone(),
            bidBaseBalance.clone(),
            bidQuoteBalance.clone(),
            tradedBidBaseAmount,
            bidOrderHead.price
        ).apply { bidOrderHead.id }
        tradingResults.add(bidResult)

        CoreEngine.tradingResults.get().add(BalanceChangedEvent(askResult.baseBalance))
        CoreEngine.tradingResults.get().add(BalanceChangedEvent(askResult.quoteBalance))
        CoreEngine.tradingResults.get().add(BalanceChangedEvent(bidResult.baseBalance))
        CoreEngine.tradingResults.get().add(BalanceChangedEvent(bidResult.quoteBalance))
        CoreEngine.tradingResults.get().add(OrderChangedEvent(askResult.remainOrder))
        CoreEngine.tradingResults.get().add(OrderChangedEvent(bidResult.remainOrder))
    }
}
