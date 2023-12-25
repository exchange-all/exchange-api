package com.exchange.job;

import com.exchange.job.common.OrderBookEventType;
import com.exchange.job.common.WindowSize;
import com.exchange.job.order.TradingResultAccumulator;
import com.exchange.job.order.TradingResultAggregator;
import com.exchange.job.order.TradingResultMapper;
import com.exchange.job.serde.CloudEventDeserializerSchema;
import io.cloudevents.CloudEvent;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author uuhnaut69
 */
public class TradingStreamAggregation {

    /**
     * Main method for running Trading Stream Aggregation
     *
     * @param args arguments
     * @throws Exception if error
     */
    public static void main(String[] args) throws Exception {
        final var parameterTool = ParameterTool.fromArgs(args);
        final var env = StreamExecutionEnvironment.getExecutionEnvironment();

        final var kafkaSource = KafkaSource.<Tuple2<CloudEvent, Long>>builder()
                .setBootstrapServers(parameterTool.get("kafka.bootstrap-servers", "localhost:19092"))
                .setTopics(parameterTool.get("kafka.topics", "order-book-reply"))
                .setGroupId(parameterTool.get("kafka.group-id", "flink-trading-stream-agg"))
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
                .setDeserializer(KafkaRecordDeserializationSchema.of(new CloudEventDeserializerSchema()))
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

        Arrays.stream(WindowSize.values())
                // Filter window size that is configured
                .filter(windowSize -> getWindowSizeTimeMap().containsKey(windowSize))
                .forEach(windowSize -> {
                            var config = getWindowSizeTimeMap().get(windowSize);
                            var windowTime = config.f0;
                            var kafkaOutputTopic = config.f1;

                            var windowedTradingResultDataStream = reKeyedTradingResultDataStream
                                    .window(TumblingProcessingTimeWindows.of(windowTime))
                                    .aggregate(new TradingResultAggregator(windowSize))
                                    .setParallelism(1);

                            windowedTradingResultDataStream
                                    .addSink(JdbcSink.sink(
                                            "INSERT INTO windowed_trades " +
                                                    "(window_type, trading_pair_id, open_price, close_price, high_price, low_price, timestamp) " +
                                                    "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING",
                                            (statement, aggResult) -> {
                                                statement.setString(1, aggResult.getWindowSize().getValue());
                                                statement.setString(2, aggResult.getTradingPairId());
                                                statement.setBigDecimal(3, aggResult.getOpenPrice());
                                                statement.setBigDecimal(4, aggResult.getClosePrice());
                                                statement.setBigDecimal(5, aggResult.getHighPrice());
                                                statement.setBigDecimal(6, aggResult.getLowPrice());
                                                statement.setLong(7, aggResult.getTimestamp());
                                            },
                                            JdbcExecutionOptions.builder()
                                                    .withBatchSize(1000)
                                                    .withBatchIntervalMs(200)
                                                    .withMaxRetries(5)
                                                    .build(),
                                            new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                                                    .withUrl(parameterTool.get("jdbc.url", "jdbc:postgresql://localhost:5433/exchange"))
                                                    .withDriverName(parameterTool.get("jdbc.driver-class-name", "org.postgresql.Driver"))
                                                    .withUsername(parameterTool.get("jdbc.username", "postgres"))
                                                    .withPassword(parameterTool.get("jdbc.password", "postgres"))
                                                    .build()
                                    ))
                                    .setParallelism(1)
                                    .name(String.format("Sink %s Windowed Trading Result to Database", windowSize.getValue()));

                            final var kafkaSink = KafkaSink.<TradingResultAccumulator>builder()
                                    .setBootstrapServers(parameterTool.get("kafka.bootstrap-servers", "localhost:19092"))
                                    .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                                            .setTopic(kafkaOutputTopic)
                                            .setValueSerializationSchema(new JsonSerializationSchema<TradingResultAccumulator>())
                                            .build()
                                    )
                                    .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                                    .build();

                            windowedTradingResultDataStream
                                    .sinkTo(kafkaSink)
                                    .setParallelism(1)
                                    .name(String.format("Sink %s Windowed Trading Result to Kafka", windowSize.getValue()));
                        }
                );

        env.execute("Trading Stream Aggregation");
    }

    /**
     * Get WindowType and Time pair
     *
     * @return Map of WindowType and its config
     */
    private static Map<WindowSize, Tuple2<Time, String>> getWindowSizeTimeMap() {
        var windowSizeTimeMap = new EnumMap<WindowSize, Tuple2<Time, String>>(WindowSize.class);
        windowSizeTimeMap.put(WindowSize.ONE_SECOND, new Tuple2<>(Time.seconds(1), "market-data.windowed-trades-1s"));
        windowSizeTimeMap.put(WindowSize.ONE_MINUTE, new Tuple2<>(Time.minutes(1), "market-data.windowed-trades-1m"));
        windowSizeTimeMap.put(WindowSize.THREE_MINUTE, new Tuple2<>(Time.minutes(3), "market-data.windowed-trades-3m"));
        windowSizeTimeMap.put(WindowSize.FIVE_MINUTE, new Tuple2<>(Time.minutes(5), "market-data.windowed-trades-5m"));
        windowSizeTimeMap.put(WindowSize.FIFTEEN_MINUTE, new Tuple2<>(Time.minutes(15), "market-data.windowed-trades-15m"));
        windowSizeTimeMap.put(WindowSize.THIRTY_MINUTE, new Tuple2<>(Time.minutes(30), "market-data.windowed-trades-30m"));
        windowSizeTimeMap.put(WindowSize.ONE_HOUR, new Tuple2<>(Time.hours(1), "market-data.windowed-trades-1h"));
        windowSizeTimeMap.put(WindowSize.TWO_HOUR, new Tuple2<>(Time.hours(2), "market-data.windowed-trades-2h"));
        windowSizeTimeMap.put(WindowSize.FOUR_HOUR, new Tuple2<>(Time.hours(4), "market-data.windowed-trades-4h"));
        windowSizeTimeMap.put(WindowSize.SIX_HOUR, new Tuple2<>(Time.hours(6), "market-data.windowed-trades-6h"));
        windowSizeTimeMap.put(WindowSize.EIGHT_HOUR, new Tuple2<>(Time.hours(8), "market-data.windowed-trades-8h"));
        windowSizeTimeMap.put(WindowSize.TWELVE_HOUR, new Tuple2<>(Time.hours(12), "market-data.windowed-trades-12h"));
        windowSizeTimeMap.put(WindowSize.ONE_DAY, new Tuple2<>(Time.days(1), "market-data.windowed-trades-1d"));
        windowSizeTimeMap.put(WindowSize.THREE_DAY, new Tuple2<>(Time.days(3), "market-data.windowed-trades-3d"));
        windowSizeTimeMap.put(WindowSize.ONE_WEEK, new Tuple2<>(Time.days(7), "market-data.windowed-trades-1w"));
        windowSizeTimeMap.put(WindowSize.ONE_MONTH, new Tuple2<>(Time.days(30), "market-data.windowed-trades-1M"));
        return windowSizeTimeMap;
    }
}
