package com.exchange.exchange.domain.balance

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

/**
 *
 * @author uuhnaut69
 *
 */
// --- Request ---
data class CreateBalanceRequest(

    @Schema(description = "Currency", example = "USDT")
    @NotNull(message = "CURRENCY_NOT_NULL")
    val currency: String,
)

data class DepositRequest(

    @Schema(description = "Currency", example = "USDT")
    @NotNull(message = "CURRENCY_NOT_NULL")
    val currency: String,

    @NotNull(message = "AMOUNT_NOT_NULL")
    @DecimalMin(
        value = "0.0", inclusive = false,
        message = "AMOUNT_MUST_BE_GREATER_THAN_ZERO"
    )
    val amount: BigDecimal,
)

data class WithdrawRequest(
    @Schema(description = "Currency", example = "USDT")
    @NotNull(message = "CURRENCY_NOT_NULL")
    val currency: String,

    @NotNull(message = "AMOUNT_NOT_NULL")
    @DecimalMin(
        value = "0.0", inclusive = false,
        message = "AMOUNT_MUST_BE_GREATER_THAN_ZERO"
    )
    val amount: BigDecimal,
)

// --- Response ---
data class CreateBalanceResponse(
    @Schema(description = "Balance id", example = "4d5558a7-6baa-4530-8e9c-b2c0eaa6c9e4")
    val balanceId: String,
)

data class DepositResponse(
    @Schema(description = "Balance id", example = "4d5558a7-6baa-4530-8e9c-b2c0eaa6c9e4")
    val balanceId: String,
)

data class WithdrawResponse(
    @Schema(description = "Balance id", example = "4d5558a7-6baa-4530-8e9c-b2c0eaa6c9e4")
    val balanceId: String,
)
