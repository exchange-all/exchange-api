package com.exchange.exchange.domain.balance

import com.exchange.exchange.core.CloudEventUtils
import com.exchange.exchange.core.Response
import com.exchange.exchange.domain.user.UserEntity
import com.exchange.exchange.exception.BadRequestException
import com.exchange.exchange.exception.InternalServerErrorException
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.stereotype.Service
import java.net.URI
import java.util.*

/**
 *
 * @author uuhnaut69
 *
 */
@Service
class BalanceService(
    @Value("\${kafka.order-book.request-topic}") private val topic: String,
    private val template: ReplyingKafkaTemplate<String, CloudEvent, CloudEvent>,
) {

    suspend fun createBalance(
        currentUser: UserEntity,
        createBalanceRequest: CreateBalanceRequest,
    ): Response<CreateBalanceResponse> {
        val createBalanceCommandEvent = CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create(CloudEventUtils.EVENT_SOURCE))
            .withType(BalanceCommandType.CREATE_BALANCE.type)
            .withData(
                CloudEventUtils.serializeData(
                    CreateBalanceCommand(
                        currentUser.id!!,
                        createBalanceRequest.currencyId,
                    )
                )
            ).build()

        val record = ProducerRecord(this.topic, createBalanceCommandEvent.id, createBalanceCommandEvent)

        val eventResult = this.template.sendAndReceive(record).await()
        return when (eventResult.value().type) {
            BalanceEventType.CREATE_BALANCE_SUCCESS.type -> {
                val event = CloudEventUtils.getReplyEventData(
                    eventResult.value(), BalanceCreated::class.java
                )
                Response.success(CreateBalanceResponse(event!!.balanceId))
            }

            BalanceEventType.CREATE_BALANCE_FAIL.type -> {
                throw BadRequestException(CloudEventUtils.getReplyEventError(eventResult.value())!!)
            }

            else -> throw InternalServerErrorException()
        }
    }

    suspend fun depositBalance(
        currentUser: UserEntity,
        depositRequest: DepositRequest,
    ): Response<DepositResponse> {
        val depositCommandEvent = CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create(CloudEventUtils.EVENT_SOURCE))
            .withType(BalanceCommandType.DEPOSIT_BALANCE.type)
            .withData(
                CloudEventUtils.serializeData(
                    DepositCommand(
                        currentUser.id!!,
                        depositRequest.accountId,
                        depositRequest.amount,
                    )
                )
            ).build()

        val record = ProducerRecord(this.topic, depositCommandEvent.id, depositCommandEvent)

        val eventResult = this.template.sendAndReceive(record).await()
        return when (eventResult.value().type) {
            BalanceEventType.DEPOSIT_BALANCE_SUCCESS.type -> {
                val event = CloudEventUtils.getReplyEventData(
                    eventResult.value(), BalanceDeposited::class.java
                )
                Response.success(DepositResponse(event!!.balanceId))
            }

            BalanceEventType.DEPOSIT_BALANCE_FAIL.type -> {
                throw BadRequestException(CloudEventUtils.getReplyEventError(eventResult.value())!!)
            }

            else -> throw InternalServerErrorException()
        }
    }

    suspend fun withdrawBalance(
        currentUser: UserEntity,
        withdrawRequest: WithdrawRequest,
    ): Response<WithdrawResponse> {
        val withdrawCommandEvent = CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create(CloudEventUtils.EVENT_SOURCE))
            .withType(BalanceCommandType.WITHDRAW_BALANCE.type)
            .withData(
                CloudEventUtils.serializeData(
                    WithdrawCommand(
                        currentUser.id!!,
                        withdrawRequest.accountId,
                        withdrawRequest.amount,
                    )
                )
            ).build()

        val record = ProducerRecord(this.topic, withdrawCommandEvent.id, withdrawCommandEvent)

        val eventResult = this.template.sendAndReceive(record).await()
        return when (eventResult.value().type) {
            BalanceEventType.WITHDRAW_BALANCE_SUCCESS.type -> {
                val event = CloudEventUtils.getReplyEventData(
                    eventResult.value(), BalanceWithdrawn::class.java
                )
                Response.success(WithdrawResponse(event!!.balanceId))
            }

            BalanceEventType.WITHDRAW_BALANCE_FAIL.type -> {
                throw BadRequestException(CloudEventUtils.getReplyEventError(eventResult.value())!!)
            }

            else -> throw InternalServerErrorException()
        }
    }
}
