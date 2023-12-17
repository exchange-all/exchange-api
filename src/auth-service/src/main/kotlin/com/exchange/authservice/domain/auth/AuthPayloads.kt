package com.exchange.authservice.domain.auth

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 *
 * @author uuhnaut69
 *
 */
abstract class LoginRequest

data class LoginWithEmailRequest(
    @Schema(
        example = "testuser@gmail.com",
        required = true,
    )
    @NotBlank(message = "EMAIL_IS_REQUIRED")
    @Email(message = "EMAIL_IS_INVALID")
    val email: String,

    @NotBlank(message = "PASSWORD_IS_REQUIRED")
    val password: String,
) : LoginRequest()

abstract class RegisterRequest

data class RegisterWithEmailRequest(
    @Schema(
        example = "testuser@gmail.com",
        required = true,
    )
    @NotBlank(message = "EMAIL_IS_REQUIRED")
    @Email(message = "EMAIL_IS_INVALID")
    val email: String,

    @NotBlank(message = "PASSWORD_IS_REQUIRED")
    val password: String,
) : RegisterRequest()
