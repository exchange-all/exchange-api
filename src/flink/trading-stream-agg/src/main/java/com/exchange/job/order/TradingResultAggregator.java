package com.exchange.job.order;

import com.exchange.job.common.TradingResult;
import com.exchange.job.common.WindowSize;
import org.apache.flink.api.common.functions.AggregateFunction;

/**
 * @author uuhnaut69
 */
public class TradingResultAggregator implements AggregateFunction<TradingResult, TradingResultAccumulator, TradingResultAccumulator> {

    private final WindowSize windowSize;

    public TradingResultAggregator(WindowSize windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public TradingResultAccumulator createAccumulator() {
        return new TradingResultAccumulator();
    }

    @Override
    public TradingResultAccumulator add(
            TradingResult value,
            TradingResultAccumulator accumulator
    ) {
        if (accumulator.getWindowSize() == null) {
            accumulator.setWindowSize(this.windowSize);
        }

        if (accumulator.getTimestamp() == null) {
            accumulator.setTimestamp(value.getTimestamp());
        }

        if (accumulator.getTradingPairId() == null) {
            accumulator.setTradingPairId(value.getRemainOrder().getTradingPairId());
        }

        if (accumulator.getOpenPrice() == null) {
            accumulator.setOpenPrice(value.getTradedPrice());
        }

        if (accumulator.getHighPrice() == null || value.getTradedPrice().compareTo(accumulator.getHighPrice()) > 0) {
            accumulator.setHighPrice(value.getTradedPrice());
        }

        if (accumulator.getLowPrice() == null || value.getTradedPrice().compareTo(accumulator.getLowPrice()) < 0) {
            accumulator.setLowPrice(value.getTradedPrice());
        }

        accumulator.setClosePrice(value.getTradedPrice());

        return accumulator;
    }

    @Override
    public TradingResultAccumulator getResult(TradingResultAccumulator accumulator) {
        return accumulator;
    }

    @Override
    public TradingResultAccumulator merge(
            TradingResultAccumulator a,
            TradingResultAccumulator b
    ) {
        var mergedAccumulator = new TradingResultAccumulator();
        mergedAccumulator.setOpenPrice(a.getOpenPrice() != null ? a.getOpenPrice() : b.getOpenPrice());
        mergedAccumulator.setClosePrice(b.getClosePrice() != null ? b.getClosePrice() : a.getClosePrice());
        mergedAccumulator.setHighPrice(a.getHighPrice() != null && b.getHighPrice() != null ? a.getHighPrice().max(b.getHighPrice()) : null);
        mergedAccumulator.setLowPrice(a.getLowPrice() != null && b.getLowPrice() != null ? a.getLowPrice().min(b.getLowPrice()) : null);
        mergedAccumulator.setTimestamp(a.getTimestamp() != null ? a.getTimestamp() : b.getTimestamp());
        return mergedAccumulator;
    }
}
