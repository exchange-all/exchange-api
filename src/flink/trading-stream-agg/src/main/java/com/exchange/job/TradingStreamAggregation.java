package com.exchange.job;

import com.exchange.job.common.OrderBookEventType;
import com.exchange.job.common.WindowSize;
import com.exchange.job.order.TradingResultAggregator;
import com.exchange.job.order.TradingResultMapper;
import com.exchange.job.serde.CloudEventDeserializerSchema;
import io.cloudevents.CloudEvent;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
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

        Arrays.stream(WindowSize.values()).forEach(windowSize -> {
                    var windowedTradingResultDataStream = reKeyedTradingResultDataStream
                            .window(TumblingProcessingTimeWindows.of(getWindowSizeTimeMap().get(windowSize)))
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
                }
        );

        env.execute("Trading Stream Aggregation");
    }

    /**
     * Get WindowType and Time pair
     *
     * @return Map of WindowType and Time
     */
    private static Map<WindowSize, Time> getWindowSizeTimeMap() {
        var windowSizeTimeMap = new EnumMap<WindowSize, Time>(WindowSize.class);
        windowSizeTimeMap.put(WindowSize.ONE_SECOND, Time.seconds(1));
        windowSizeTimeMap.put(WindowSize.ONE_MINUTE, Time.minutes(1));
        windowSizeTimeMap.put(WindowSize.THREE_MINUTE, Time.minutes(3));
        windowSizeTimeMap.put(WindowSize.FIVE_MINUTE, Time.minutes(5));
        windowSizeTimeMap.put(WindowSize.FIFTEEN_MINUTE, Time.minutes(15));
        windowSizeTimeMap.put(WindowSize.THIRTY_MINUTE, Time.minutes(30));
        windowSizeTimeMap.put(WindowSize.ONE_HOUR, Time.hours(1));
        windowSizeTimeMap.put(WindowSize.TWO_HOUR, Time.hours(2));
        windowSizeTimeMap.put(WindowSize.FOUR_HOUR, Time.hours(4));
        windowSizeTimeMap.put(WindowSize.SIX_HOUR, Time.hours(6));
        windowSizeTimeMap.put(WindowSize.EIGHT_HOUR, Time.hours(8));
        windowSizeTimeMap.put(WindowSize.TWELVE_HOUR, Time.hours(12));
        windowSizeTimeMap.put(WindowSize.ONE_DAY, Time.days(1));
        windowSizeTimeMap.put(WindowSize.THREE_DAY, Time.days(3));
        windowSizeTimeMap.put(WindowSize.ONE_WEEK, Time.days(7));
        windowSizeTimeMap.put(WindowSize.ONE_MONTH, Time.days(30));
        return windowSizeTimeMap;
    }
}
