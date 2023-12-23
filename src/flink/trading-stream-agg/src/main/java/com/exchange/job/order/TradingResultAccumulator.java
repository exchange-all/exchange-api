package com.exchange.job.order;

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
public class TradingResultAccumulator {

    private String tradingPairId;

    private BigDecimal openPrice;

    private BigDecimal closePrice;

    private BigDecimal highPrice;

    private BigDecimal lowPrice;

    private Long timestamp;

}
