package com.exchange.exchange.domain.auth

import com.exchange.exchange.domain.user.UserEntity
import com.exchange.exchange.exception.UnauthorizedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 *
 * @author uuhnaut69
 *
 */
@Service
class AuthService {

    private val authServiceProviderMap: MutableMap<String, AuthServiceProvider> = mutableMapOf()

    @Autowired
    fun setAuthServiceProviderMap(
        authServiceProviderMap: Map<String, AuthServiceProvider>,
    ) {
        this.authServiceProviderMap.putAll(authServiceProviderMap)
    }

    /**
     * Register user
     *
     * @param request [RegisterRequest]
     */
    suspend fun register(request: RegisterRequest) {
        when (request) {
            is RegisterWithEmailRequest -> this.authServiceProviderMap["emailAndPasswordAuthServiceProvider"]?.register(
                request
            )

            else -> throw UnauthorizedException()
        }
    }

    /**
     * Login user
     *
     * @param request [LoginRequest]
     * @return [UserEntity]
     */
    suspend fun login(request: LoginRequest): UserEntity {
        return when (request) {
            is LoginWithEmailRequest -> {
                this.authServiceProviderMap["emailAndPasswordAuthServiceProvider"]?.login(
                    request
                ) ?: throw UnauthorizedException()
            }

            else -> throw UnauthorizedException()
        }
    }

}
