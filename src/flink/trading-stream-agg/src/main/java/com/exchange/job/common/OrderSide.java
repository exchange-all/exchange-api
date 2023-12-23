package com.exchange.job.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author uuhnaut69
 */
@Getter
@AllArgsConstructor
public enum OrderSide {
    BUY("BUY"),
    SELL("SELL");

    private final String value;
}
