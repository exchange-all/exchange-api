package com.exchange.authservice.security

import com.exchange.authservice.domain.user.UserEntity
import com.exchange.authservice.exception.UnauthorizedException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author uuhnaut69
 *
 */
@Component
class AuthenticationManager : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val userEntity = authentication.principal as UserEntity

        if (userEntity.isEnabled.not()
            || userEntity.isAccountNonLocked.not()
            || userEntity.isAccountNonExpired.not()
            || userEntity.isCredentialsNonExpired.not()
        ) {
            return Mono.error { UnauthorizedException() }
        }

        return Mono.just(authentication)
    }
}
