package com.exchange.orderbook.model.entity

import java.io.Serializable

/**
 * @author thaivc
 * @since 2023
 */
interface Identifiable<T : Serializable> {
  val id: T
}
