package com.exchange.exchange.config

import io.cloudevents.spring.messaging.CloudEventMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


/**
 *
 * @author uuhnaut69
 *
 */
@Configuration
class CloudEventMessageConverterConfiguration {

    @Bean
    fun cloudEventMessageConverter() = CloudEventMessageConverter()
}
