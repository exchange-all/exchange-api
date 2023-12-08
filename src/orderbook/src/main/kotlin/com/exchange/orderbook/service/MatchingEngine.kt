package com.exchange.orderbook.service

import com.exchange.orderbook.model.TradingPair
import com.exchange.orderbook.model.currencyPair
import com.exchange.orderbook.model.entity.OrderEntity
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
        orders.forEach {
            val pair = tradingPairInMemoryRepository.findById(it.tradingPairId)
            if (pair != null) {
                tradingPairs.putIfAbsent(
                    currencyPair(pair.baseCurrency, pair.quoteCurrency),
                    TradingPair(pair.baseCurrency, pair.quoteCurrency)
                )!!.addOrder(it)
            }
        }
    }

    fun addOrder(order: OrderEntity) {
        tradingPairInMemoryRepository.findById(order.tradingPairId)!!.let {
            orderInMemoryRepository.upsert(order)
            tradingPairs.putIfAbsent(
                currencyPair(it.baseCurrency, it.quoteCurrency),
                TradingPair(it.baseCurrency, it.quoteCurrency)
            )!!.addOrder(order)
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
            val askMinPrice = tradingPair.asks.peek()
            val bidMaxPrice = tradingPair.bids.peek()

            if (askMinPrice == null || bidMaxPrice == null) break@ASK_BID_PRICE_COMPARE
            if (askMinPrice.value > bidMaxPrice.value) break@ASK_BID_PRICE_COMPARE

            val askOrders: TreeSet<OrderEntity> = tradingPair.sellOrders[askMinPrice]!!
            val bidOrders: TreeSet<OrderEntity> = tradingPair.buyOrders[bidMaxPrice]!!

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
                tradingPair.sellOrders.remove(askMinPrice)
                tradingPair.asks.remove(askMinPrice)
            }

            // check and remove bids head
            if (bidOrders.isEmpty()) {
                tradingPair.buyOrders.remove(askMinPrice)
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
        tradingResults.add(TradingResult().apply {
            id = askOrderHead.id.toString()
            remainOrder = askOrderHead.clone()
            baseBalance = askBaseBalance.clone()
            quoteBalance = askQuoteBalance.clone()
            tradedAmount = tradedBaseAmount
            tradedPrice = askOrderHead.price
        })
        tradingResults.add(TradingResult().apply {
            id = askOrderHead.id.toString()
            remainOrder = bidOrderHead.clone()
            baseBalance = bidBaseBalance.clone()
            quoteBalance = bidQuoteBalance.clone()
            tradedAmount = tradedBaseAmount
            tradedPrice = bidOrderHead.price
        })
    }
}
