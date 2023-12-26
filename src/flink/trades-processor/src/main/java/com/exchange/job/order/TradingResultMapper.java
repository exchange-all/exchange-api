package com.exchange.job.order;

import com.exchange.job.common.TradingResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;

import java.util.Optional;

import static io.cloudevents.core.CloudEventUtils.mapData;

/**
 * @author uuhnaut69
 */
public class TradingResultMapper implements MapFunction<Tuple3<String, CloudEvent, Long>, TradingResult> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TradingResult map(Tuple3<String, CloudEvent, Long> value) throws Exception {
        var id = value.f0;
        var cloudEvent = value.f1;
        var messageTimestamp = value.f2;

        var cloudEventData = Optional.ofNullable(mapData(
                cloudEvent,
                PojoCloudEventDataMapper.from(objectMapper, TradingResult.class)
        ));

        return cloudEventData.map(eventData -> {
            var tradingResult = eventData.getValue();
            tradingResult.setId(id);
            tradingResult.setTimestamp(messageTimestamp);
            return tradingResult;
        }).orElse(null);
    }
}
