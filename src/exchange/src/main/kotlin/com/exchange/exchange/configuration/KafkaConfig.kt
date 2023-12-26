package com.exchange.exchange.configuration

import io.cloudevents.CloudEvent
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate


/**
 *
 * @author uuhnaut69
 *
 */
@Configuration
class KafkaConfig {

    // -- Kafka Common Config -- //
    @Bean
    fun kafkaTemplate(pf: ProducerFactory<String, CloudEvent>): KafkaTemplate<String, CloudEvent> {
        return KafkaTemplate(pf)
    }

    // -- Kafka Order Book Config -- //
    @Bean
    fun orderBookReplyingTemplate(
        pf: ProducerFactory<String, CloudEvent>,
        orderBookReplyingContainer: ConcurrentMessageListenerContainer<String, CloudEvent>,
    ): ReplyingKafkaTemplate<String, CloudEvent, CloudEvent> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(pf, orderBookReplyingContainer)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        return replyingKafkaTemplate
    }

    @Bean
    fun orderBookReplyingContainer(
        @Value("\${kafka.order-book.reply-topic}") topic: String,
        @Value("\${kafka.order-book.consumer.group-id}") groupId: String,
        environment: Environment,
        kafkaTemplate: KafkaTemplate<String, CloudEvent>,
        containerFactory: ConcurrentKafkaListenerContainerFactory<String, CloudEvent>,
    ): ConcurrentMessageListenerContainer<String, CloudEvent> {
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
