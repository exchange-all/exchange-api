package com.exchange.job.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author uuhnaut69
 */
@Getter
@AllArgsConstructor
public enum OrderBookEventType {
    TRADING_RESULT("TRADING_RESULT");
    private final String value;
}
