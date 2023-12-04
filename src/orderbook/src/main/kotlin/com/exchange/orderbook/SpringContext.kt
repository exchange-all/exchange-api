package com.exchange.orderbook

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

/**
 * @author thaivc
 * @since 2023
 */
@Service
class SpringContext(private val applicationContext: ApplicationContext) {
  companion object {
    private lateinit var context: ApplicationContext

    fun getBean(beanName: String): Any {
      return context.getBean(beanName)
    }

    fun <T> getBean(beanClass: Class<T>): T {
      return context.getBean(beanClass)
    }
  }

  init {
    context = this.applicationContext
  }
}
