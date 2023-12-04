package com.exchange.exchange.domain.balance

import com.exchange.exchange.core.CommandType
import com.exchange.exchange.core.Response
import com.exchange.exchange.domain.user.UserEntity
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.stereotype.Service
import java.util.*

/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
@Service
class BalanceService(
    @Value("\${kafka.order-book.request-topic}")
    private val topic: String,
    private val template: ReplyingKafkaTemplate<String, Any, Response<*>>,
) {

    suspend fun deposit(
        currentUser: UserEntity,
        depositRequest: DepositRequest,
    ): Response<DepositEvent> {
        val record = ProducerRecord<String, Any>(
            this.topic, UUID.randomUUID().toString(), DepositCommand(
                currentUser.id!!,
                depositRequest.accountId,
                depositRequest.amount,
            )
        )
        record.headers().add("type", CommandType.DEPOSIT.name.toByteArray())

        val eventResult = this.template.sendAndReceive(record).await()
        return Response(
            eventResult.value().success,
            jacksonObjectMapper().convertValue(
                eventResult.value().data,
                DepositEvent::class.java
            ),
            eventResult.value().errors,
        )
    }

    suspend fun withdraw(
        currentUser: UserEntity,
        withdrawRequest: WithdrawRequest,
    ): Response<WithdrawEvent> {
        val record = ProducerRecord<String, Any>(
            this.topic, UUID.randomUUID().toString(), WithdrawCommand(
                currentUser.id!!,
                withdrawRequest.accountId,
                withdrawRequest.amount,
            )
        )
        record.headers().add("type", CommandType.WITHDRAW.name.toByteArray())

        val eventResult = this.template.sendAndReceive(record).await()
        return Response(
            eventResult.value().success,
            jacksonObjectMapper().convertValue(
                eventResult.value().data,
                WithdrawEvent::class.java
            ),
            eventResult.value().errors,
        )
    }
}
