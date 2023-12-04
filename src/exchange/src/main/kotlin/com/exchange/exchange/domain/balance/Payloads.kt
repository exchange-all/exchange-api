package com.exchange.exchange.domain.balance

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
data class CreateBalanceRequest(

    @Schema(description = "Currency id", example = "656bf22f37f9d256dcd962a2")
    @NotNull(message = "CURRENCY_ID_NOT_NULL")
    val currencyId: String,
)

data class DepositRequest(

    @Schema(description = "Balance id", example = "656bf22f37f9d256dcd962a2")
    @NotNull(message = "BALANCE_ID_NOT_NULL")
    val accountId: String,

    @NotNull(message = "AMOUNT_NOT_NULL")
    @DecimalMin(
        value = "0.0", inclusive = false,
        message = "AMOUNT_MUST_BE_GREATER_THAN_ZERO"
    )
    val amount: BigDecimal,
)

data class WithdrawRequest(
    @Schema(description = "Balance id", example = "656bf22f37f9d256dcd962a2")
    @NotNull(message = "BALANCE_ID_NOT_NULL")
    val accountId: String,

    @NotNull(message = "AMOUNT_NOT_NULL")
    @DecimalMin(
        value = "0.0", inclusive = false,
        message = "AMOUNT_MUST_BE_GREATER_THAN_ZERO"
    )
    val amount: BigDecimal,
)
