package com.exchange.job.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author uuhnaut69
 */
@Getter
@AllArgsConstructor
public enum WindowSize {
    ONE_SECOND("ONE_SECOND"),
    ONE_MINUTE("ONE_MINUTE"),
    THREE_MINUTE("THREE_MINUTE"),
    FIVE_MINUTE("FIVE_MINUTE"),
    FIFTEEN_MINUTE("FIFTEEN_MINUTE"),
    THIRTY_MINUTE("THIRTY_MINUTE"),
    ONE_HOUR("ONE_HOUR"),
    TWO_HOUR("TWO_HOUR"),
    FOUR_HOUR("FOUR_HOUR"),
    SIX_HOUR("SIX_HOUR"),
    EIGHT_HOUR("EIGHT_HOUR"),
    TWELVE_HOUR("TWELVE_HOUR"),
    ONE_DAY("ONE_DAY"),
    THREE_DAY("THREE_DAY"),
    ONE_WEEK("ONE_WEEK"),
    ONE_MONTH("ONE_MONTH"),
    ;

    private final String value;

}
