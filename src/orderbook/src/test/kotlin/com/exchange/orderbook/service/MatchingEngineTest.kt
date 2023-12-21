package com.exchange.orderbook.service

import com.exchange.orderbook.model.entity.OrderEntity
import com.exchange.orderbook.model.entity.OrderType
import com.exchange.orderbook.model.entity.Status
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */

class MatchingEngineTest {

    @Test
    fun `test matching one head-bid and one head-ask `() {
        val asks: LinkedHashMap<String, OrderEntity> = LinkedHashMap()

        // 10, 50, 30, 20, 40
        listOf(10, 50, 30, 20, 40).forEach {
            val id = UUID.randomUUID().toString()
            asks[id] = OrderEntity().apply {
                this.id = id
                this.amount = BigDecimal(it)
                this.availableAmount = BigDecimal(it)
                this.price = BigDecimal.ONE
                this.status = Status.OPEN
                userId = UUID.randomUUID().toString()
                tradingPairId = UUID.randomUUID().toString()
                type = OrderType.SELL
            }
        }

        val bids: LinkedHashMap<String, OrderEntity> = LinkedHashMap()
        // 25, 40, 10
        listOf(25, 40, 10).forEach {
            val id = UUID.randomUUID().toString()
            bids[id] = OrderEntity().apply {
                this.id = id
                this.amount = BigDecimal(it)
                this.availableAmount = BigDecimal(it)
                this.price = BigDecimal.ONE
                this.status = Status.OPEN
                userId = UUID.randomUUID().toString()
                tradingPairId = UUID.randomUUID().toString()
                type = OrderType.BUY
            }
        }

        while (true) {
            if (asks.isEmpty() || bids.isEmpty()) {
                println("END TEST!!!")
                println("remain-asks: ${jacksonObjectMapper().writeValueAsString(asks)}")
                println("remain-bids: ${jacksonObjectMapper().writeValueAsString(bids)}")
                return
            }
            val askHead = asks.entries.first().value
            val bidHead = bids.entries.first().value

            val tradedQuoteAmount = minOf(askHead.availableAmount, bidHead.availableAmount)

            print("ask ${askHead.availableAmount}, bid ${bidHead.availableAmount} -> traded: $tradedQuoteAmount -> ")

            askHead.availableAmount = askHead.availableAmount.subtract(tradedQuoteAmount)
            bidHead.availableAmount = bidHead.availableAmount.subtract(tradedQuoteAmount)

            println("ask ${askHead.availableAmount}, bid ${bidHead.availableAmount}")

            if (askHead.availableAmount <= BigDecimal.ZERO) {
                asks.remove(askHead.id)
            }

            if (bidHead.availableAmount <= BigDecimal.ZERO) {
                bids.remove(bidHead.id)
            }
        }
    }

}
