package com.exchange.orderbook.model.event

import com.exchange.orderbook.model.entity.Cloneable
import org.springframework.http.HttpStatus

/**
 * @author thaivc
 * @since 2023
 */
open class EventResponse {
  var id: String? = null
  var status: Int? = null
  lateinit var event: IEvent

  companion object {
    fun ok(event: IEvent, data: Cloneable): SuccessResponse {
      return SuccessResponse().apply {
        this.id = event.id
        this.event = event

        /** always clone data to avoid any changing */
        this.data = data.clone()
        this.status = HttpStatus.OK.value()
      }
    }

    fun fail(event: IEvent, error: String): FailResponse {
      return FailResponse().apply {
        this.id = event.id
        this.event = event
        this.error = error
        this.status = HttpStatus.NOT_ACCEPTABLE.value()
      }
    }
  }
}

class SuccessResponse() : EventResponse() {
  lateinit var data: Cloneable
}

class FailResponse() : EventResponse() {
  lateinit var error: String
}
