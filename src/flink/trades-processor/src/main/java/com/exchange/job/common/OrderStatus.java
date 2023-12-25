package com.exchange.job.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author uuhnaut69
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {
    OPEN("OPEN"),
    CLOSED("CLOSED");

    private final String value;
}
