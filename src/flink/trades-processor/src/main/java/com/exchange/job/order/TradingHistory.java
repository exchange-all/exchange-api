package com.exchange.job.order;

import com.exchange.job.common.OrderSide;
import com.exchange.job.common.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author uuhnaut69
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradingHistory {

    private String id;

    private String userId;

    private String tradingPairId;

    private String orderId;

    private BigDecimal amount;

    private BigDecimal availableAmount;

    private BigDecimal price;

    private OrderSide type;

    private OrderStatus status;

    private BigDecimal tradedAmount;

    private BigDecimal tradedPrice;

    private Long tradedAt;

}
