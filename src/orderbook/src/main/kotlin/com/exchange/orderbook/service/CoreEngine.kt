package com.exchange.orderbook.service

import com.exchange.orderbook.model.constants.MessageError
import com.exchange.orderbook.model.entity.BalanceEntity
import com.exchange.orderbook.model.event.*
import com.exchange.orderbook.repository.memory.BalanceInMemoryRepository
import java.math.BigDecimal
import java.util.*
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * @author thaivc
 * @since 2023
 */
@Service
class CoreEngine(
    private val balanceInMemoryRepository: BalanceInMemoryRepository,
    private val outboundListener: OutboundListener
) {

  companion object {
    private val log = LoggerFactory.getLogger(CoreEngine::class.java)
  }

  private val handlers: Map<Class<out IEvent>, (IEvent) -> EventResponse> =
      mapOf(
          CreateBalanceEvent::class.java to ::onCreateBalanceEvent,
          DepositBalanceEvent::class.java to ::onDepositBalanceEvent,
          WithdrawBalanceEvent::class.java to ::onWithdrawBalanceEvent)

  fun consumeEvents(records: ConsumerRecords<String, IEvent>) {
    if (records.isEmpty) return

    val results =
        records.map { record ->
          handlers[record.value()::class.java]?.invoke(record.value())
              ?: EventResponse.fail(record.value(), MessageError.EVENT_NOT_FOUND)
        }
    outboundListener.enqueue(records.last().offset(), results)
  }

  private fun onCreateBalanceEvent(e: IEvent): EventResponse {
    val event = e as CreateBalanceEvent
    val balance =
        balanceInMemoryRepository.findByUserIdAndCurrency(
            UUID.fromString(event.userId), event.currency)
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
    return EventResponse.ok(event, entity)
  }

  private fun onDepositBalanceEvent(e: IEvent): EventResponse {
    val event = e as DepositBalanceEvent
    val balance =
        balanceInMemoryRepository.findByUserIdAndCurrency(
            UUID.fromString(event.userId), event.currency)
            ?: return EventResponse.fail(event, MessageError.BALANCE_NOT_FOUND)

    balance.availableAmount = balance.availableAmount.add(event.amount)
    balanceInMemoryRepository.upsert(balance)
    return EventResponse.ok(event, balance)
  }

  private fun onWithdrawBalanceEvent(e: IEvent): EventResponse {
    val event = e as WithdrawBalanceEvent
    val balance =
        balanceInMemoryRepository.findByUserIdAndCurrency(
            UUID.fromString(event.userId), event.currency)
            ?: return EventResponse.fail(event, MessageError.BALANCE_NOT_FOUND)

    balance.availableAmount = balance.availableAmount.subtract(event.amount)
    balanceInMemoryRepository.upsert(balance)
    return EventResponse.ok(event, balance)
  }
}
