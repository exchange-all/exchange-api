package com.exchange.exchange.config

import com.exchange.exchange.core.Response
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate


/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
@Configuration
class KafkaConfig {

    // -- Kafka Common Config -- //
    @Bean
    fun kafkaTemplate(pf: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(pf)
    }

    // -- Kafka Order Book Config -- //
    @Bean
    fun orderBookReplyingTemplate(
        pf: ProducerFactory<String, Any>,
        orderBookReplyingContainer: ConcurrentMessageListenerContainer<String, Response<*>>,
    ): ReplyingKafkaTemplate<String, Any, Response<*>> {
        return ReplyingKafkaTemplate(pf, orderBookReplyingContainer)
    }

    @Bean
    fun orderBookReplyingContainer(
        @Value("\${kafka.order-book.reply-topic}") topic: String,
        @Value("\${kafka.order-book.consumer.group-id}") groupId: String,
        environment: Environment,
        kafkaTemplate: KafkaTemplate<String, Any>,
        containerFactory: ConcurrentKafkaListenerContainerFactory<String, Response<*>>,
    ): ConcurrentMessageListenerContainer<String, Response<*>> {

        /**
         * If the application is running in standalone mode, the reply template is set to the
         * [KafkaTemplate] created in this method, that is used for sending mock replies.
         */
        environment.activeProfiles.contains("standalone").let {
            containerFactory.setReplyTemplate(kafkaTemplate)
        }

        val container = containerFactory.createContainer(topic)
        container.containerProperties.setGroupId(groupId)
        container.isAutoStartup = false
        return container
    }

    @Bean
    fun orderBookRequests(
        @Value("\${kafka.order-book.request-topic}") topic: String,
        @Value("\${kafka.order-book.partition-count}") partitions: Int,
        @Value("\${kafka.order-book.replication-factor}") replicationFactor: Int,
    ): NewTopic {
        return TopicBuilder.name(topic).partitions(partitions).replicas(replicationFactor).build()
    }

    @Bean
    fun orderBookReplies(
        @Value("\${kafka.order-book.reply-topic}") topic: String,
        @Value("\${kafka.order-book.partition-count}") partitions: Int,
        @Value("\${kafka.order-book.replication-factor}") replicationFactor: Int,
    ): NewTopic {
        return TopicBuilder.name(topic).partitions(partitions).replicas(replicationFactor).build()
    }
}
