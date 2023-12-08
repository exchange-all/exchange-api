package com.exchange.orderbook.model.entity

import org.springframework.data.mongodb.core.mapping.Document

/**
 * @author thaivc
 * @since 2023
 */
@Document("offsets")
class OffsetEntity {

    lateinit var id: String
    lateinit var topic: String
    var partition: Int = 0
    var offset: Long = 0
}
