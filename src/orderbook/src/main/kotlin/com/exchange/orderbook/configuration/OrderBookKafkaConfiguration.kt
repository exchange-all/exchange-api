package com.exchange.orderbook.configuration

import com.exchange.orderbook.model.event.IEvent
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author thaivc
 * @since 2023
 */
@Configuration
class OrderBookKafkaConfiguration {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var brokers: String
    @Value("\${order-book.topic}")
    private lateinit var orderBookTopic: String
    @Value("\${order-book.reply-topic}")
    private lateinit var replyOrderBookTopic: String

    @Bean
    fun orderBookConsumerProvider(): (Int) -> KafkaConsumer<String, IEvent> {
        return { maxPollRecord ->
            KafkaConsumer<String, IEvent>(
                mapOf(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to brokers,
                    ConsumerConfig.GROUP_ID_CONFIG to "order-book.group",
                    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                    ConsumerConfig.ISOLATION_LEVEL_CONFIG to "read_committed",
                    ConsumerConfig.MAX_POLL_RECORDS_CONFIG to maxPollRecord,
                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to EventDeserializer::class.java
                )
            )
        }
    }

    @Bean
    fun orderBookTopic(): NewTopic {
        return NewTopic(orderBookTopic, 1, 1)
    }

    @Bean
    fun replyOrderBookTopic(): NewTopic {
        return NewTopic(replyOrderBookTopic, 1, 1)
    }
}
