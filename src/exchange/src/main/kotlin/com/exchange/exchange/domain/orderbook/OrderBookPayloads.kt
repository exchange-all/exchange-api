package com.exchange.exchange.domain.orderbook

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

/**
 * @author uuhnaut69
 *
 */

//-- Request
data class AskLimitOrderRequest(

    @NotNull(message = "BASE_CURRENCY_NOT_NULL")
    val baseCurrency: String,

    @NotNull(message = "QUOTE_CURRENCY_NOT_NULL")
    val quoteCurrency: String,

    @NotNull(message = "PRICE_NOT_NULL")
    @DecimalMin(
        value = "0.0", inclusive = false,
        message = "PRICE_MUST_BE_GREATER_THAN_ZERO"
    )
    val price: BigDecimal,

    @NotNull(message = "AMOUNT_NOT_NULL")
    @DecimalMin(
        value = "0.0", inclusive = false,
        message = "AMOUNT_MUST_BE_GREATER_THAN_ZERO"
    )
    val amount: BigDecimal
)

data class BidLimitOrderRequest(

    @NotNull(message = "BASE_CURRENCY_NOT_NULL")
    val baseCurrency: String,

    @NotNull(message = "QUOTE_CURRENCY_NOT_NULL")
    val quoteCurrency: String,

    @NotNull(message = "PRICE_NOT_NULL")
    @DecimalMin(
        value = "0.0", inclusive = false,
        message = "PRICE_MUST_BE_GREATER_THAN_ZERO"
    )
    val price: BigDecimal,

    @NotNull(message = "AMOUNT_NOT_NULL")
    @DecimalMin(
        value = "0.0", inclusive = false,
        message = "AMOUNT_MUST_BE_GREATER_THAN_ZERO"
    )
    val amount: BigDecimal
)

data class CancelAskLimitOrderRequest(
    @NotNull(message = "ORDER_ID_NOT_NULL")
    val orderId: String,

    @NotNull(message = "BASE_CURRENCY_NOT_NULL")
    val baseCurrency: String,

    @NotNull(message = "QUOTE_CURRENCY_NOT_NULL")
    val quoteCurrency: String,
)

data class CancelBidLimitOrderRequest(
    @NotNull(message = "ORDER_ID_NOT_NULL")
    val orderId: String,

    @NotNull(message = "BASE_CURRENCY_NOT_NULL")
    val baseCurrency: String,

    @NotNull(message = "QUOTE_CURRENCY_NOT_NULL")
    val quoteCurrency: String,
)

//-- Response
data class AskLimitOrderResponse(
    val id: String,
    val userId: String,
    val tradingPairId: String,
    val amount: BigDecimal,
    val availableAmount: BigDecimal,
    val price: BigDecimal,
    val type: String,
    val status: String,
)

data class BidLimitOrderResponse(
    val id: String,
    val userId: String,
    val tradingPairId: String,
    val amount: BigDecimal,
    val availableAmount: BigDecimal,
    val price: BigDecimal,
    val type: String,
    val status: String,
)
