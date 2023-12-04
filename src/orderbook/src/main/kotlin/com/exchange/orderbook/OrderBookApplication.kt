package com.exchange.orderbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OrderBookApplication

fun main(args: Array<String>) {
	runApplication<OrderBookApplication>(*args)
}
