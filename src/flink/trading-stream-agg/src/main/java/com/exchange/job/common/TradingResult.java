package com.exchange.job.common;

import lombok.*;

import java.math.BigDecimal;

/**
 * @author uuhnaut69
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradingResult implements Event {

    private BigDecimal tradedPrice;

    private BigDecimal tradedAmount;

    private RemainOrder remainOrder;

    private BalanceInfo baseBalance;

    private BalanceInfo quoteBalance;

    private Long timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemainOrder {
        private String id;

        private String userId;

        private String tradingPairId;

        private BigDecimal amount;

        private BigDecimal availableAmount;

        private BigDecimal price;

        private OrderSide type;

        private OrderStatus status;

        private Long priority;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceInfo {
        private String id;

        private String userId;

        private String currency;

        private BigDecimal availableAmount;

        private BigDecimal lockAmount;
    }
}
