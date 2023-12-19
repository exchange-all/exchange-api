package com.exchange.job;

import com.exchange.job.serde.TradingDeserializerSchema;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author uuhnaut69
 */
public class TradingStreamAggregation {
    public static void main(String[] args) throws Exception {
        final var env = StreamExecutionEnvironment.getExecutionEnvironment();

        final var kafkaSource = KafkaSource.<JsonNode>builder()
                .setBootstrapServers("localhost:19092")
                .setTopics("order-book-reply")
                .setGroupId("trading-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(new TradingDeserializerSchema())
                .build();

        final var tradingStream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Order Book Trading Raw Data"
        );

        tradingStream.print();

        env.execute("Trading Stream Aggregation");
    }
}
