package com.exchange.authservice.domain.auth

import com.exchange.authservice.domain.user.UserEntity

/**
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
