package com.exchange.exchange.domain.orderbook

import com.exchange.exchange.core.Response
import com.exchange.exchange.domain.user.UserEntity
import com.exchange.exchange.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author uuhnaut69
 *
 */
@Tag(name = "Order Book", description = "Order Book API")
@RestController
@RequestMapping("/api/v1/order-book")
class OrderBookController(
    private val orderBookService: OrderBookService
) {

    @Operation(
        summary = "Create new order",
        description = "Create new order"
    )
    @PostMapping("/create-order")
    suspend fun createNewOrder(
        @CurrentUser currentUser: UserEntity,
        @RequestBody @Valid createOrderRequest: CreateOrderRequest,
    ): Response<CreateOrderResponse> {
        return this.orderBookService.createNewOrder(currentUser, createOrderRequest)
    }

    @Operation(
        summary = "Cancel order",
        description = "Cancel order"
    )
    @PostMapping("/cancel-order")
    suspend fun cancelOrder(
        @CurrentUser currentUser: UserEntity,
        @RequestBody @Valid request: CancelOrderRequest,
    ): Response<CancelOrderResponse> {
        return this.orderBookService.cancelOrder(currentUser, request)
    }
}
