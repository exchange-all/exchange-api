package com.exchange.exchange.domain.orderbook

import com.exchange.exchange.core.CloudEventUtils
import com.exchange.exchange.core.ObjectMapper
import com.exchange.exchange.core.Response
import com.exchange.exchange.domain.user.UserEntity
import com.exchange.exchange.exception.BadRequestException
import com.exchange.exchange.exception.InternalServerErrorException
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.stereotype.Service
import java.net.URI
import java.util.*

/**
 * @author uuhnaut69
 *
 */
@Service
class OrderBookService(
    @Value("\${kafka.order-book.request-topic}") private val orderBookTopic: String,
    private val template: ReplyingKafkaTemplate<String, CloudEvent, CloudEvent>,
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OrderBookService::class.java)
    }

    suspend fun createAskLimitOrder(
        currentUser: UserEntity,
        createAskLimitOrderRequest: AskLimitOrderRequest
    ): Response<AskLimitOrderResponse> {
        val id = UUID.randomUUID().toString()
        val createAskLimitCommandEvent = CloudEventBuilder.v1()
            .withId(id)
            .withSource(URI.create(CloudEventUtils.EVENT_SOURCE))
            .withType(OrderBookCommandType.CREATE_ASK_LIMIT_ORDER.type)
            .withData(
                CloudEventUtils.serializeData(
                    CreateAskLimitOrderCommand(
                        id,
                        currentUser.id,
                        createAskLimitOrderRequest.baseCurrency,
                        createAskLimitOrderRequest.quoteCurrency,
                        createAskLimitOrderRequest.price,
                        createAskLimitOrderRequest.amount
                    )
                )
            ).build()

        val record = ProducerRecord(this.orderBookTopic, createAskLimitCommandEvent.id, createAskLimitCommandEvent)

        val eventResult = this.template.sendAndReceive(record).await()
        return when (eventResult.value().type) {
            OrderBookEventType.CREATE_ASK_LIMIT_ORDER_SUCCESS.type -> {
                val event = CloudEventUtils.getReplyEventData(
                    eventResult.value(), AskLimitOrderCreated::class.java
                )
                Response.success(
                    ObjectMapper.instance.convertValue(
                        event!!,
                        AskLimitOrderResponse::class.java
                    )
                )
            }

            OrderBookEventType.CREATE_ASK_LIMIT_ORDER_FAILED.type -> {
                throw BadRequestException(CloudEventUtils.getReplyEventError(eventResult.value())!!)
            }

            else -> throw InternalServerErrorException()
        }
    }

    suspend fun createBidLimitOrder(
        currentUser: UserEntity,
        createBidLimitOrderRequest: BidLimitOrderRequest
    ): Response<BidLimitOrderResponse> {
        val id = UUID.randomUUID().toString()
        val createBidLimitCommandEvent = CloudEventBuilder.v1()
            .withId(id)
            .withSource(URI.create(CloudEventUtils.EVENT_SOURCE))
            .withType(OrderBookCommandType.CREATE_BID_LIMIT_ORDER.type)
            .withData(
                CloudEventUtils.serializeData(
                    CreateBidLimitOrderCommand(
                        id,
                        currentUser.id,
                        createBidLimitOrderRequest.baseCurrency,
                        createBidLimitOrderRequest.quoteCurrency,
                        createBidLimitOrderRequest.price,
                        createBidLimitOrderRequest.amount
                    )
                )
            ).build()

        val record = ProducerRecord(this.orderBookTopic, createBidLimitCommandEvent.id, createBidLimitCommandEvent)

        val eventResult = this.template.sendAndReceive(record).await()
        return when (eventResult.value().type) {
            OrderBookEventType.CREATE_BID_LIMIT_ORDER_SUCCESS.type -> {
                val event = CloudEventUtils.getReplyEventData(
                    eventResult.value(), BidLimitOrderCreated::class.java
                )
                Response.success(
                    ObjectMapper.instance.convertValue(
                        event!!,
                        BidLimitOrderResponse::class.java
                    )
                )
            }

            OrderBookEventType.CREATE_BID_LIMIT_ORDER_FAILED.type -> {
                throw BadRequestException(CloudEventUtils.getReplyEventError(eventResult.value())!!)
            }

            else -> throw InternalServerErrorException()
        }

    }

    suspend fun cancelAskLimitOrder(
        currentUser: UserEntity,
        cancelAskLimitOrderRequest: CancelAskLimitOrderRequest
    ): Response<AskLimitOrderResponse> {
        val id = UUID.randomUUID().toString()
        val cancelAskLimitOrderCommandEvent = CloudEventBuilder.v1()
            .withId(id)
            .withSource(URI.create(CloudEventUtils.EVENT_SOURCE))
            .withType(OrderBookCommandType.CANCEL_ASK_LIMIT_ORDER.type)
            .withData(
                CloudEventUtils.serializeData(
                    CancelAskLimitOrderCommand(
                        id,
                        currentUser.id,
                        cancelAskLimitOrderRequest.orderId,
                        cancelAskLimitOrderRequest.baseCurrency,
                        cancelAskLimitOrderRequest.quoteCurrency
                    )
                )
            )
            .build()

        val record =
            ProducerRecord(this.orderBookTopic, cancelAskLimitOrderCommandEvent.id, cancelAskLimitOrderCommandEvent)

        val eventResult = this.template.sendAndReceive(record).await()
        return when (eventResult.value().type) {
            OrderBookEventType.CANCEL_ASK_LIMIT_ORDER_SUCCESS.type -> {
                val event = CloudEventUtils.getReplyEventData(
                    eventResult.value(), AskLimitOrderCancelled::class.java
                )
                Response.success(
                    ObjectMapper.instance.convertValue(
                        event!!,
                        AskLimitOrderResponse::class.java
                    )
                )
            }

            OrderBookEventType.CANCEL_ASK_LIMIT_ORDER_FAILED.type -> {
                throw BadRequestException(CloudEventUtils.getReplyEventError(eventResult.value())!!)
            }

            else -> throw InternalServerErrorException()
        }
    }

    suspend fun cancelBidLimitOrder(
        currentUser: UserEntity,
        cancelBidLimitOrderRequest: CancelBidLimitOrderRequest
    ): Response<BidLimitOrderResponse> {
        val id = UUID.randomUUID().toString()
        val cancelBidLimitOrderCommandEvent = CloudEventBuilder.v1()
            .withId(id)
            .withSource(URI.create(CloudEventUtils.EVENT_SOURCE))
            .withType(OrderBookCommandType.CANCEL_BID_LIMIT_ORDER.type)
            .withData(
                CloudEventUtils.serializeData(
                    CancelBidLimitOrderCommand(
                        id,
                        currentUser.id,
                        cancelBidLimitOrderRequest.orderId,
                        cancelBidLimitOrderRequest.baseCurrency,
                        cancelBidLimitOrderRequest.quoteCurrency
                    )
                )
            )
            .build()

        val record =
            ProducerRecord(this.orderBookTopic, cancelBidLimitOrderCommandEvent.id, cancelBidLimitOrderCommandEvent)

        val eventResult = this.template.sendAndReceive(record).await()
        return when (eventResult.value().type) {
            OrderBookEventType.CANCEL_BID_LIMIT_ORDER_SUCCESS.type -> {
                val event = CloudEventUtils.getReplyEventData(
                    eventResult.value(), BidLimitOrderCancelled::class.java
                )
                Response.success(
                    ObjectMapper.instance.convertValue(
                        event!!,
                        BidLimitOrderResponse::class.java
                    )
                )
            }

            OrderBookEventType.CANCEL_BID_LIMIT_ORDER_FAILED.type -> {
                throw BadRequestException(CloudEventUtils.getReplyEventError(eventResult.value())!!)
            }

            else -> throw InternalServerErrorException()
        }
    }
}
