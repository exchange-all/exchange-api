package com.exchange.exchange.configuration

import io.cloudevents.spring.messaging.CloudEventMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


/**
 *
 * @author uuhnaut69
 *
 */
@Configuration
class CloudEventMessageConverterConfig {

    @Bean
    fun cloudEventMessageConverter() = CloudEventMessageConverter()
}
