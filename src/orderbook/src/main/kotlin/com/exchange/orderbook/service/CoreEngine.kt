package com.exchange.orderbook.service

import com.exchange.orderbook.model.constants.MessageError
import com.exchange.orderbook.model.entity.BalanceEntity
import com.exchange.orderbook.model.entity.OrderEntity
import com.exchange.orderbook.model.event.*
import com.exchange.orderbook.repository.memory.BalanceInMemoryRepository
import com.exchange.orderbook.repository.memory.TradingPairInMemoryRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
@Service
class CoreEngine(
  private val balanceInMemoryRepository: BalanceInMemoryRepository,
  private val outboundListener: OutboundListener,
  private val matchingEngine: MatchingEngine,
  private val tradingPairInMemoryRepository: TradingPairInMemoryRepository
) {

  companion object {
    private val log = LoggerFactory.getLogger(CoreEngine::class.java)

    /**
     * Result of trading
     */
    private val tradingResults: ThreadLocal<List<EventResponse>> = ThreadLocal.withInitial { null }

    private val consumerRecord: ThreadLocal<ConsumerRecord<String, IEvent>> =
      ThreadLocal.withInitial { null }
  }

  /**
   * Map each event with its handler
   */
  private val handlers: Map<Class<out IEvent>, (IEvent) -> EventResponse> =
    mapOf(
      CreateBalanceEvent::class.java to ::onCreateBalanceEvent,
      DepositBalanceEvent::class.java to ::onDepositBalanceEvent,
      WithdrawBalanceEvent::class.java to ::onWithdrawBalanceEvent,
      AskLimitOrderEvent::class.java to ::onAskLimitOrderEvent,
      BidLimitOrderEvent::class.java to ::onBidLimitOrderEvent
    )

  /**
   * Consume chunk of kafka messages
   */
  fun consumeEvents(records: ConsumerRecords<String, IEvent>) {
    if (records.isEmpty) return

    val results =
      records.flatMap { record ->
        consumerRecord.set(record)
        // handle request
        val result = handlers[record.value()::class.java]?.invoke(record.value())
          ?: EventResponse.fail(record.value(), MessageError.EVENT_NOT_FOUND)
        val results = mutableListOf(result)

        // get trading result if any
        if (tradingResults.get() != null) {
          results.addAll(tradingResults.get())
          tradingResults.remove()
        }
        consumerRecord.remove()
        results
      }
    outboundListener.enqueue(records.last().offset(), results)
  }

  private fun onCreateBalanceEvent(e: IEvent): EventResponse {
    val event = e as CreateBalanceEvent
    val balance =
      balanceInMemoryRepository.findByUserIdAndCurrency(
        UUID.fromString(event.userId), event.currency
      )
    if (balance != null) {
      return EventResponse.fail(event, MessageError.BALANCE_EXISTED)
    }
    val entity =
      BalanceEntity().apply {
        id = UUID.fromString(event.id)
        userId = UUID.fromString(event.userId)
        currency = event.currency
        availableAmount = BigDecimal.ZERO
        lockAmount = BigDecimal.ZERO
      }
    balanceInMemoryRepository.upsert(entity)
    return EventResponse.ok(event, entity.clone())
  }

  private fun onDepositBalanceEvent(e: IEvent): EventResponse {
    val event = e as DepositBalanceEvent
    val balance =
      balanceInMemoryRepository.findByUserIdAndCurrency(
        UUID.fromString(event.userId), event.currency
      )
        ?: return EventResponse.fail(event, MessageError.BALANCE_NOT_FOUND)

    balance.availableAmount = balance.availableAmount.add(event.amount)
    balanceInMemoryRepository.upsert(balance)
    return EventResponse.ok(event, balance.clone())
  }

  private fun onWithdrawBalanceEvent(e: IEvent): EventResponse {
    val event = e as WithdrawBalanceEvent
    val balance =
      balanceInMemoryRepository.findByUserIdAndCurrency(
        UUID.fromString(event.userId), event.currency
      )
        ?: return EventResponse.fail(event, MessageError.BALANCE_NOT_FOUND)

    balance.availableAmount = balance.availableAmount.subtract(event.amount)
    balanceInMemoryRepository.upsert(balance)
    return EventResponse.ok(event, balance.clone())
  }

  private fun onAskLimitOrderEvent(e: IEvent): EventResponse {
    val event = e as AskLimitOrderEvent

    // check trading pair
    val tradingPair =
      tradingPairInMemoryRepository.findByCurrencyPair(event.baseCurrency, event.quoteCurrency)
        ?: return EventResponse.fail(event, MessageError.TRADING_PAIR_NOT_FOUND)

    // handle asks
    val balance = balanceInMemoryRepository.findByUserIdAndCurrency(
      UUID.fromString(event.userId),
      event.baseCurrency
    )
      ?: return EventResponse.fail(event, MessageError.BALANCE_NOT_FOUND)

    val totalSell = event.amount

    if (balance.availableAmount < totalSell) {
      return EventResponse.fail(event, MessageError.BALANCE_NOT_ENOUGH)
    }
    balance.availableAmount = balance.availableAmount.minus(totalSell)
    balance.lockAmount = balance.lockAmount.plus(totalSell)

    val order = OrderEntity.sell(
      UUID.fromString(event.id),
      UUID.fromString(event.userId),
      tradingPair.id,
      event.amount,
      event.price,
      consumerRecord.get().offset()
    )

    matchingEngine.addOrder(order)
    matchingEngine.matching(event.baseCurrency, event.quoteCurrency).takeIf { it.isNotEmpty() }
      ?.let { tradingResults.set(it) }

    return EventResponse.ok(event, order.clone())
  }

  private fun onBidLimitOrderEvent(e: IEvent): EventResponse {
    val event = e as BidLimitOrderEvent

    // check trading pair
    val tradingPair =
      tradingPairInMemoryRepository.findByCurrencyPair(event.baseCurrency, event.quoteCurrency)
        ?: return EventResponse.fail(event, MessageError.TRADING_PAIR_NOT_FOUND)

    // handle bids
    val balance = balanceInMemoryRepository.findByUserIdAndCurrency(
      UUID.fromString(event.userId),
      event.quoteCurrency
    )
      ?: return EventResponse.fail(event, MessageError.BALANCE_NOT_FOUND)

    val totalBuy = event.amount.multiply(event.price)
    if (balance.availableAmount < totalBuy) {
      return EventResponse.fail(event, MessageError.BALANCE_NOT_ENOUGH)
    }
    balance.availableAmount = balance.availableAmount.minus(totalBuy)
    balance.lockAmount = balance.lockAmount.plus(totalBuy)

    val order = OrderEntity.buy(
      UUID.fromString(event.id),
      UUID.fromString(event.userId),
      tradingPair.id,
      event.amount,
      event.price,
      consumerRecord.get().offset()
    )

    matchingEngine.addOrder(order)
    matchingEngine.matching(event.baseCurrency, event.quoteCurrency).takeIf { it.isNotEmpty() }
      ?.let { tradingResults.set(it) }
    return EventResponse.ok(event, order.clone())
  }

}
