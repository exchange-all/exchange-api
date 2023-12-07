package com.exchange.exchange.security

import com.exchange.exchange.security.session.SessionAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession

/**
 *
 * @author uuhnaut69
 *
 */
@Configuration
@EnableRedisWebSession
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun securityWebFilterChain(
            http: ServerHttpSecurity,
            authenticationManager: AuthenticationManager,
    ): SecurityWebFilterChain {
        // @formatter:off
        return http.apply {
            authorizeExchange {
                it.pathMatchers(
                    // Health Check
                    "/actuator/**",
                    // -- Swagger
                    "/v2/api-docs",
                    "/swagger-resources",
                    "/swagger-resources/**",
                    "/configuration/ui",
                    "/configuration/security",
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger.json/**",
                    "/swagger",
                    // Public API
                    "/api/v1/auth/login-with-email",
                    "/api/v1/auth/register-with-email",
                ).permitAll()
                it.anyExchange().authenticated()
            }
            .addFilterAt(
                sessionAuthenticationFilter(
                    authenticationManager
                ), SecurityWebFiltersOrder.AUTHENTICATION
            )
            csrf {
                it.disable()
            }
            formLogin {
                it.disable()
            }
            httpBasic {
                it.disable()
            }
        }.build()
        // @formatter:on
    }

    @Bean
    fun sessionAuthenticationFilter(authenticationManager: AuthenticationManager): AuthenticationWebFilter {
        val authenticationWebFilter = AuthenticationWebFilter(authenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(
                SessionAuthenticationConverter()
        )
        return authenticationWebFilter
    }

}
