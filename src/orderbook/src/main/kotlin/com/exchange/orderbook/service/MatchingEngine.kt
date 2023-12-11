package com.exchange.orderbook.service

import com.exchange.orderbook.model.TradingPair
import com.exchange.orderbook.model.currencyPair
import com.exchange.orderbook.model.entity.OrderEntity
import com.exchange.orderbook.model.entity.Status
import com.exchange.orderbook.model.event.BalanceChangedEvent
import com.exchange.orderbook.model.event.OrderChangedEvent
import com.exchange.orderbook.model.event.TradingResult
import com.exchange.orderbook.repository.memory.BalanceInMemoryRepository
import com.exchange.orderbook.repository.memory.OrderInMemoryRepository
import com.exchange.orderbook.repository.memory.TradingPairInMemoryRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
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
        val tradingResults: MutableList<TradingResult> = mutableListOf()

        ASK_BID_PRICE_COMPARE@
        while (true) {
            if (tradingPair.asks.isEmpty() || tradingPair.bids.isEmpty()) break@ASK_BID_PRICE_COMPARE

            val askMinPrice = tradingPair.asks.firstKey()
            val bidMaxPrice = tradingPair.bids.firstKey()
            if (askMinPrice.value > bidMaxPrice.value) break@ASK_BID_PRICE_COMPARE

            val askOrders: TreeSet<OrderEntity> = tradingPair.asks[askMinPrice]!!
            val bidOrders: TreeSet<OrderEntity> = tradingPair.bids[bidMaxPrice]!!

            ASK_BID_AMOUNT_COMPARE@
            while (true) {
                if (askOrders.isEmpty() || bidOrders.isEmpty())
                    break@ASK_BID_AMOUNT_COMPARE

                val askOrderHead: OrderEntity = askOrders.first()
                val bidOrderHead: OrderEntity = bidOrders.first()
                val askSell = askOrderHead.availableAmount
                val bidBuy = bidOrderHead.availableAmount

                when {
                    (askSell > bidBuy) -> handleAskOverBid(
                        askOrders,
                        bidOrders,
                        askOrderHead,
                        bidOrderHead,
                        tradingPair,
                        tradingResults
                    )

                    (askSell < bidBuy) -> handleBidOverAsk(
                        askOrders,
                        bidOrders,
                        askOrderHead,
                        bidOrderHead,
                        tradingPair,
                        tradingResults
                    )

                    else -> handleAskEqualsBid(
                        askOrders,
                        bidOrders,
                        askOrderHead,
                        bidOrderHead,
                        tradingPair,
                        tradingResults
                    )
                }
            }
            // check and remove asks head
            if (askOrders.isEmpty()) {
                tradingPair.asks.remove(askMinPrice)
            }

            // check and remove bids head
            if (bidOrders.isEmpty()) {
                tradingPair.bids.remove(askMinPrice)
            }
        }
        return tradingResults
    }

    private fun handleAskOverBid(
        askOrders: TreeSet<OrderEntity>,
        bidOrders: TreeSet<OrderEntity>,
        askOrderHead: OrderEntity,
        bidOrderHead: OrderEntity,
        tradingPair: TradingPair,
        tradingResults: MutableList<TradingResult>
    ) {
        updateBalances(askOrderHead, bidOrderHead, tradingPair, tradingResults)

        // update ask-order and bid-order
        askOrderHead.availableAmount =
            askOrderHead.availableAmount.subtract(bidOrderHead.availableAmount)
        bidOrderHead.availableAmount = BigDecimal.ZERO
        bidOrderHead.status = Status.CLOSED
        CoreEngine.tradingResults.get().add(OrderChangedEvent(askOrderHead.clone()))
        CoreEngine.tradingResults.get().add(OrderChangedEvent(bidOrderHead.clone()))

        bidOrders.remove(bidOrderHead)
    }

    private fun handleBidOverAsk(
        askOrders: TreeSet<OrderEntity>,
        bidOrders: TreeSet<OrderEntity>,
        askOrderHead: OrderEntity,
        bidOrderHead: OrderEntity,
        tradingPair: TradingPair,
        tradingResults: MutableList<TradingResult>
    ) {
        updateBalances(askOrderHead, bidOrderHead, tradingPair, tradingResults)

        // update ask-order and bid-order
        bidOrderHead.availableAmount =
            bidOrderHead.availableAmount.subtract(askOrderHead.availableAmount)
        askOrderHead.availableAmount = BigDecimal.ZERO
        askOrderHead.status = Status.CLOSED
        CoreEngine.tradingResults.get().add(OrderChangedEvent(askOrderHead.clone()))
        CoreEngine.tradingResults.get().add(OrderChangedEvent(bidOrderHead.clone()))

        askOrders.remove(askOrderHead)
    }

    private fun handleAskEqualsBid(
        askOrders: TreeSet<OrderEntity>,
        bidOrders: TreeSet<OrderEntity>,
        askOrderHead: OrderEntity,
        bidOrderHead: OrderEntity,
        tradingPair: TradingPair,
        tradingResults: MutableList<TradingResult>
    ) {
        updateBalances(askOrderHead, bidOrderHead, tradingPair, tradingResults)

        // update ask-order and bid-order
        askOrderHead.availableAmount = BigDecimal.ZERO
        bidOrderHead.availableAmount = BigDecimal.ZERO
        askOrderHead.status = Status.CLOSED
        bidOrderHead.status = Status.CLOSED
        CoreEngine.tradingResults.get().add(OrderChangedEvent(askOrderHead.clone()))
        CoreEngine.tradingResults.get().add(OrderChangedEvent(bidOrderHead.clone()))

        bidOrders.remove(bidOrderHead)
        askOrders.remove(askOrderHead)
    }

    private fun updateBalances(
        askOrderHead: OrderEntity,
        bidOrderHead: OrderEntity,
        tradingPair: TradingPair,
        tradingResults: MutableList<TradingResult>
    ) {
        // determine traded amount
        val tradedBaseAmount = minOf(
            askOrderHead.availableAmount,
            bidOrderHead.availableAmount
        )

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
            askQuoteBalance.availableAmount.add(tradedBaseAmount.multiply(askOrderHead.price))
        askBaseBalance.lockAmount = askBaseBalance.lockAmount.subtract(tradedBaseAmount)

        // add base-currency availableAmount and subtract quote-currency lockedAmount for ask
        val bidBaseBalance = balanceInMemoryRepository.findByUserIdAndCurrency(
            bidOrderHead.userId,
            tradingPair.baseCurrency
        )!!
        val bidQuoteBalance = balanceInMemoryRepository.findByUserIdAndCurrency(
            bidOrderHead.userId,
            tradingPair.quoteCurrency
        )!!
        bidBaseBalance.availableAmount = bidBaseBalance.availableAmount.add(tradedBaseAmount)
        bidQuoteBalance.lockAmount =
            bidQuoteBalance.lockAmount.subtract(tradedBaseAmount.multiply(askOrderHead.price))

        // add result
        tradingResults.add(TradingResult(
            askOrderHead.clone(),
            askBaseBalance.clone(),
            askQuoteBalance.clone(),
            tradedBaseAmount,
            askOrderHead.price
        ).apply { askOrderHead.id })

        tradingResults.add(TradingResult(
            bidOrderHead.clone(),
            bidBaseBalance.clone(),
            bidQuoteBalance.clone(),
            tradedBaseAmount,
            bidOrderHead.price
        ).apply { bidOrderHead.id })

        CoreEngine.tradingResults.get().add(BalanceChangedEvent(askBaseBalance.clone()))
        CoreEngine.tradingResults.get().add(BalanceChangedEvent(askQuoteBalance.clone()))
        CoreEngine.tradingResults.get().add(BalanceChangedEvent(bidBaseBalance.clone()))
        CoreEngine.tradingResults.get().add(BalanceChangedEvent(bidQuoteBalance.clone()))
    }
}
