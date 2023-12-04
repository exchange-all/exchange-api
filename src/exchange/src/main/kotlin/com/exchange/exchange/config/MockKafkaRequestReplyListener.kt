package com.exchange.exchange.config

import com.exchange.exchange.core.Command
import com.exchange.exchange.core.Event
import com.exchange.exchange.core.Response
import com.exchange.exchange.domain.balance.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.util.*

/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
@Component
@Profile("standalone")
class MockKafkaRequestReplyListener {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(MockKafkaRequestReplyListener::class.java)
    }

    @KafkaListener(id = "standalone-group", topics = ["\${kafka.order-book.request-topic}"])
    @SendTo("\${kafka.order-book.reply-topic}")
    fun fakeReply(command: Command): Message<Response<Event>> {
        val event: Event = when (command) {
            is CreateBalanceCommand -> BalanceCreated(command.currencyId)
            is DepositCommand -> BalanceDeposited(command.accountId)
            is WithdrawCommand -> BalanceWithdrawn(command.accountId)
            // -- Add more cases here --
            else -> throw IllegalArgumentException("Unknown command type: ${command::class.java}")
        }
        LOGGER.info("Fake reply event: $event")
        return MessageBuilder.withPayload(Response.success(event)).build()
    }
}
