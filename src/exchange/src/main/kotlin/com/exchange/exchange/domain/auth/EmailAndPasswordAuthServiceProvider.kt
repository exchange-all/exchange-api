package com.exchange.exchange.domain.auth

import com.exchange.exchange.domain.user.Role
import com.exchange.exchange.domain.user.UserEntity
import com.exchange.exchange.domain.user.UserRepository
import com.exchange.exchange.exception.BadRequestException
import com.exchange.exchange.exception.UnauthorizedException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
@Service("emailAndPasswordAuthServiceProvider")
class EmailAndPasswordAuthServiceProvider(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : AuthServiceProvider {

    companion object {
        private val LOGGER =
            LoggerFactory.getLogger(EmailAndPasswordAuthServiceProvider::class.java)
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    override suspend fun register(registerRequest: RegisterRequest): UserEntity {
        val registerWithEmailRequest = registerRequest as RegisterWithEmailRequest

        val userEntity = this.convertToUserEntity(registerWithEmailRequest)

        val exists = this.userRepository.existsByEmailIgnoreCase(userEntity.email).awaitSingle()

        if (exists) {
            throw BadRequestException("EMAIL_EXISTED_ERROR")
        }

        this.userRepository.save(userEntity).awaitSingle()

        LOGGER.debug("Register user ${userEntity.username} successfully")

        return userEntity
    }

    override suspend fun login(loginRequest: LoginRequest): UserEntity {
        val localRegisterRequest = loginRequest as LoginWithEmailRequest
        val userEntity = this.userRepository.findByEmailIgnoreCase(
            localRegisterRequest.email
        ).awaitSingleOrNull() ?: throw UnauthorizedException("USER_NOT_FOUND_ERROR")

        if (!passwordEncoder.matches(localRegisterRequest.password, userEntity.password)) {
            throw UnauthorizedException("PASSWORD_NOT_MATCH_ERROR")
        }

        return userEntity
    }

    private fun convertToUserEntity(request: RegisterWithEmailRequest): UserEntity {
        return UserEntity.newUser(
            request.email.trimIndent().lowercase(),
            passwordEncoder.encode(request.password),
            listOf(Role.ROLE_USER)
        )
    }

}
