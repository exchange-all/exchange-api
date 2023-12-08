package com.exchange.exchange.domain.auth

import com.exchange.exchange.core.Response
import com.exchange.exchange.domain.user.UserEntity
import com.exchange.exchange.security.CurrentUser
import com.exchange.exchange.security.session.SessionUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.awaitSession

/**
 *
 * @author uuhnaut69
 *
 */
@Tag(name = "Authentication", description = "Authentication API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    @Operation(
        summary = "Register new user with email and password",
        description = "Register new user with email and password",
    )
    @PostMapping("/register-with-email")
    suspend fun register(@RequestBody @Valid request: RegisterWithEmailRequest): Response<Unit> {
        this.authService.register(request)
        return Response.success()
    }

    @Operation(
        summary = "Login with email and password",
        description = "Login with email and password",
    )
    @PostMapping("/login-with-email")
    suspend fun loginWithEmail(
        @RequestBody @Valid loginWithEmailRequest: LoginWithEmailRequest,
        serverWebExchange: ServerWebExchange,
    ): Response<Unit> {
        val userEntity = this.authService.login(loginWithEmailRequest)

        // Serialize user to session
        serverWebExchange.awaitSession().let {
            SessionUtils.serializeUser(it, userEntity)
        }

        return Response.success()
    }

    @Operation(
        summary = "Logout session",
        description = "Logout session",
    )
    @PostMapping("/logout")
    suspend fun logout(
        @CurrentUser userEntity: UserEntity,
        serverWebExchange: ServerWebExchange,
    ): Response<Unit> {
        serverWebExchange.awaitSession().invalidate().awaitSingleOrNull()
        return Response.success()
    }
}
