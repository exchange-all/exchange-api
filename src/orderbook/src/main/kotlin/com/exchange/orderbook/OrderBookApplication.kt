package com.exchange.orderbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class OrderBookApplication

fun main(args: Array<String>) {
	runApplication<OrderBookApplication>(*args)
}
