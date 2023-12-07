package com.exchange.exchange.security.session

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

/**
 *
 * @author uuhnaut69
 *
 */
class SessionAuthenticationConverter : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return exchange.session.mapNotNull {
            SessionUtils.deserializeUser(it)
        }.switchIfEmpty {
            Mono.empty()
        }.map {
            UsernamePasswordAuthenticationToken(
                    it,
                    null,
                    it?.authorities ?: listOf()
            )
        }
    }
}
