package com.exchange.exchange.domain.trading

import com.exchange.exchange.domain.orderbook.OrderBookEventType
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverRecord

/**
 * @author thaivc
 * @since 2023
 */
@RestController
class TradingSSEResource(
    private val kafkaReceiver: KafkaReceiver<String, String>
) {
    @GetMapping(path = ["/sse/trading"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun trading(): Flux<String> {
        val receiver: Flux<ReceiverRecord<String, String>> = kafkaReceiver.receive()
        return receiver
            .doOnNext { it.receiverOffset().acknowledge() }
            .filter {
                it.headers().headers("ce_type")?.firstOrNull()?.value()
                    ?.let { header -> String(header) } == OrderBookEventType.TRADING_RESULT.type
            }
            .map { it.value() }
    }
}
