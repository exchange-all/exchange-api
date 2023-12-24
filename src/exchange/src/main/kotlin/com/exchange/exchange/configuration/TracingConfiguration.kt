package com.exchange.exchange.configuration

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author thaivc
 * @since 2023
 */
@Configuration
class TracingConfiguration {

    @Bean
    fun otlpGrpcSpanExporter(@Value("\${tracing.url}") url: String) : OtlpGrpcSpanExporter {
        return OtlpGrpcSpanExporter.builder().setEndpoint(url).build()
    }

}
