package com.exchange.job;

import com.exchange.job.common.OrderBookEventType;
import com.exchange.job.common.WindowSize;
import com.exchange.job.order.*;
import com.exchange.job.serde.CloudEventDeserializerSchema;
import com.exchange.job.sink.RedisPubSubSink;
import io.cloudevents.CloudEvent;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
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
public class TradeProcessor {

    /**
     * Main method for running Trading Stream Aggregation
     *
     * @param args arguments
     * @throws Exception if error
     */
    public static void main(String[] args) throws Exception {
        final var parameterTool = ParameterTool.fromArgs(args);
        final var env = StreamExecutionEnvironment.getExecutionEnvironment();

        final var kafkaSource = KafkaSource.<Tuple3<String, CloudEvent, Long>>builder()
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
                .filter(data -> data.f1.getType().equals(OrderBookEventType.TRADING_RESULT.getValue()))
                .name("Mapping to Trading Result Pojo")
                .map(new TradingResultMapper());

        final var reKeyedTradingResultDataStream = tradingResultDataStream
                .name("Partitioning by Trading Pair Id")
                .keyBy(data -> data.getRemainOrder().getTradingPairId());

        // Process and sink windowed trading result to Database and Kafka
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
                                                    "(window_type, trading_pair_id, open_price, close_price, high_price, low_price, window_timestamp) " +
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
                                    .setTransactionalIdPrefix(String.format("windowed-trades-%s", windowSize.getValue().toLowerCase()))
                                    .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                                    .build();

                            windowedTradingResultDataStream
                                    .sinkTo(kafkaSink)
                                    .setParallelism(1)
                                    .name(String.format("Sink %s Windowed Trading Result to Kafka", windowSize.getValue()));

                            windowedTradingResultDataStream
                                    .addSink(new RedisPubSubSink<>(kafkaOutputTopic))
                                    .setParallelism(1)
                                    .name(String.format("Sink %s Windowed Trading Result to Redis", windowSize.getValue()));
                        }
                );

        final var tradingHistoryDataStream = tradingResultDataStream
                .name("Mapping to Trading History Pojo")
                .map(new TradingHistoryMapper());

        // Process and sink trading history to Database and Kafka
        tradingHistoryDataStream
                .addSink(JdbcSink.sink(
                        "INSERT INTO trade_histories " +
                                "(id, user_id, trading_pair_id, order_id, amount, available_amount, price, type, status, traded_amount, traded_price, traded_at) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING",
                        (statement, tradingHistory) -> {
                            statement.setString(1, tradingHistory.getId());
                            statement.setString(2, tradingHistory.getUserId());
                            statement.setString(3, tradingHistory.getTradingPairId());
                            statement.setString(4, tradingHistory.getOrderId());
                            statement.setBigDecimal(5, tradingHistory.getAmount());
                            statement.setBigDecimal(6, tradingHistory.getAvailableAmount());
                            statement.setBigDecimal(7, tradingHistory.getPrice());
                            statement.setString(8, tradingHistory.getType().getValue());
                            statement.setString(9, tradingHistory.getStatus().getValue());
                            statement.setBigDecimal(10, tradingHistory.getTradedAmount());
                            statement.setBigDecimal(11, tradingHistory.getTradedPrice());
                            statement.setLong(12, tradingHistory.getTradedAt());
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
                .name("Sink Trading History to Database");

        final var tradingHistoryKafkaSink = KafkaSink.<TradingHistory>builder()
                .setBootstrapServers(parameterTool.get("kafka.bootstrap-servers", "localhost:19092"))
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("market-data.trade-histories")
                        .setValueSerializationSchema(new JsonSerializationSchema<TradingHistory>())
                        .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .setTransactionalIdPrefix("trade-histories")
                .build();

        tradingHistoryDataStream.sinkTo(tradingHistoryKafkaSink)
                .name("Sink Trading History to Kafka")
                .setParallelism(1);

        tradingHistoryDataStream
                .addSink(new RedisPubSubSink<>("market-data.trade-histories"))
                .setParallelism(1)
                .name("Sink Trading History to Redis");

        env.execute("Trades Processor");
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
