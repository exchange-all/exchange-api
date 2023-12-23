package com.exchange.job;

import com.exchange.job.common.OrderBookEventType;
import com.exchange.job.order.TradingResultAggregator;
import com.exchange.job.order.TradingResultMapper;
import com.exchange.job.serde.CloudEventDeserializerSchema;
import io.cloudevents.CloudEvent;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * @author uuhnaut69
 */
public class TradingStreamAggregation {
    public static void main(String[] args) throws Exception {
        // TODO: Need to refactor load config to determine run on local or cluster
        final var env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());

        final var kafkaSource = KafkaSource.<Tuple2<CloudEvent, Long>>builder()
                .setBootstrapServers("localhost:19092")
                .setTopics("order-book-reply")
                .setGroupId("trading-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(
                        KafkaRecordDeserializationSchema.of(new CloudEventDeserializerSchema())
                )
                .build();

        final var orderBookDataStream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Order Book Data Stream"
        );

        final var tradingResultDataStream = orderBookDataStream
                .name("Filtering Trading Result")
                .filter(data -> data.f0.getType().equals(OrderBookEventType.TRADING_RESULT.getValue()))
                .name("Mapping to Trading Result Pojo")
                .map(new TradingResultMapper());

        final var reKeyedTradingResultDataStream = tradingResultDataStream
                .name("Partitioning by Trading Pair Id")
                .keyBy(data -> data.getRemainOrder().getTradingPairId());

        final var oneMinuteWindowedTradingResultDataStream = reKeyedTradingResultDataStream
                .window(TumblingProcessingTimeWindows.of(Time.minutes(1)))
                .aggregate(new TradingResultAggregator());

        final var fiveMinuteWindowedTradingResultDataStream = reKeyedTradingResultDataStream
                .window(TumblingProcessingTimeWindows.of(Time.minutes(5)))
                .aggregate(new TradingResultAggregator());

        final var fifteenMinuteWindowedTradingResultDataStream = reKeyedTradingResultDataStream
                .window(TumblingProcessingTimeWindows.of(Time.minutes(15)))
                .aggregate(new TradingResultAggregator());

        oneMinuteWindowedTradingResultDataStream
                .name("One Minute Windowed Trading Result")
                .print();

        fiveMinuteWindowedTradingResultDataStream
                .name("Five Minute Windowed Trading Result")
                .print();
        fifteenMinuteWindowedTradingResultDataStream
                .name("Fifteen Minute Windowed Trading Result")
                .print();

        env.execute("Trading Stream Aggregation");
    }
}
