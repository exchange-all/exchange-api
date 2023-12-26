package com.exchange.job.order;

import com.exchange.job.common.TradingResult;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @author uuhnaut69
 */
public class TradingHistoryMapper implements MapFunction<TradingResult, TradingHistory> {
    @Override
    public TradingHistory map(TradingResult value) throws Exception {
        return new TradingHistory(
                value.getId(),
                value.getRemainOrder().getUserId(),
                value.getRemainOrder().getTradingPairId(),
                value.getRemainOrder().getId(),
                value.getRemainOrder().getAmount(),
                value.getRemainOrder().getAvailableAmount(),
                value.getRemainOrder().getPrice(),
                value.getRemainOrder().getType(),
                value.getRemainOrder().getStatus(),
                value.getTradedAmount(),
                value.getTradedPrice(),
                value.getTimestamp()
        );
    }
}
