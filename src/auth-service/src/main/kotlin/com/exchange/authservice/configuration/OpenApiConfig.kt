package com.exchange.authservice.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author uuhnaut69
 *
 */
@Configuration
@ConfigurationProperties(prefix = "springdoc.open-api")
class OpenApiConfig {

    companion object {
        const val SCHEME_NAME = "cookieAuth"
        const val COOKIE_NAME = "SESSION"
    }

    @Bean
    fun openApi(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Exchange API Service")
                    .description("Exchange API Service Documentation")
                    .version(javaClass.`package`.implementationVersion)
            )
            .security(
                listOf(
                    SecurityRequirement().addList(SCHEME_NAME)
                )
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        SCHEME_NAME,
                        SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .`in`(SecurityScheme.In.COOKIE)
                            .name(COOKIE_NAME)
                    )
            )
    }
}
