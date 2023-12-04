package com.exchange.exchange.domain.auth

import com.exchange.exchange.domain.user.UserEntity

/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
interface AuthServiceProvider {

    /**
     * Register new user
     * @param registerRequest [RegisterRequest]
     */
    suspend fun register(registerRequest: RegisterRequest): UserEntity

    /**
     * Login
     * @param loginRequest [LoginRequest]
     * @return [UserEntity]
     */
    suspend fun login(loginRequest: LoginRequest): UserEntity

}
