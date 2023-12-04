package com.exchange.exchange.domain.balance

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
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
@Tag(name = "Balance", description = "Balance API")
@RestController
@RequestMapping("/api/v1/balances")
class BalanceController(
    private val balanceService: BalanceService,
) {

    @Operation(
        summary = "Create balance",
        description = "Create a new balance",
    )
    @PostMapping("/create")
    suspend fun createBalance(
        @CurrentUser currentUser: UserEntity,
        @RequestBody @Valid createBalanceRequest: CreateBalanceRequest,
    ): Response<BalanceCreated> {
        return this.balanceService.createBalance(currentUser, createBalanceRequest)
    }

    @Operation(
        summary = "Deposit balance",
        description = "Create a new deposit balance",
    )
    @PostMapping("/deposit")
    suspend fun deposit(
        @CurrentUser currentUser: UserEntity,
        @RequestBody @Valid depositRequest: DepositRequest,
    ): Response<BalanceDeposited> {
        return this.balanceService.deposit(currentUser, depositRequest)
    }

    @Operation(
        summary = "Withdraw balance",
        description = "Create a new withdraw balance",
    )
    @PostMapping("/withdraw")
    suspend fun withdraw(
        @CurrentUser currentUser: UserEntity,
        @RequestBody @Valid withdrawRequest: WithdrawRequest,
    ): Response<BalanceWithdrawn> {
        return this.balanceService.withdraw(currentUser, withdrawRequest)
    }
}
