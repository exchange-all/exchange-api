package com.exchange.job;

import com.exchange.job.common.OrderBookEventType;
import com.exchange.job.common.WindowType;
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

        Arrays.stream(WindowType.values()).forEach(windowType -> {
                    var windowedTradingResultDataStream = reKeyedTradingResultDataStream
                            .window(TumblingProcessingTimeWindows.of(getWindowTypeTimeMap().get(windowType)))
                            .aggregate(new TradingResultAggregator(windowType))
                            .setParallelism(1);

                    windowedTradingResultDataStream
                            .addSink(JdbcSink.sink(
                                    "INSERT INTO windowed_trades " +
                                            "(window_type, trading_pair_id, open_price, close_price, high_price, low_price, timestamp) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING",
                                    (statement, aggResult) -> {
                                        statement.setString(1, aggResult.getWindowType().getValue());
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
                            .name(String.format("Sink %s Windowed Trading Result to Database", windowType.getValue()));
                }
        );

        env.execute("Trading Stream Aggregation");
    }

    /**
     * Get WindowType and Time pair
     *
     * @return Map of WindowType and Time
     */
    private static Map<WindowType, Time> getWindowTypeTimeMap() {
        var windowTypeMap = new EnumMap<WindowType, Time>(WindowType.class);
        windowTypeMap.put(WindowType.ONE_SECOND, Time.seconds(1));
        windowTypeMap.put(WindowType.ONE_MINUTE, Time.minutes(1));
        windowTypeMap.put(WindowType.THREE_MINUTE, Time.minutes(3));
        windowTypeMap.put(WindowType.FIVE_MINUTE, Time.minutes(5));
        windowTypeMap.put(WindowType.FIFTEEN_MINUTE, Time.minutes(15));
        windowTypeMap.put(WindowType.THIRTY_MINUTE, Time.minutes(30));
        windowTypeMap.put(WindowType.ONE_HOUR, Time.hours(1));
        windowTypeMap.put(WindowType.TWO_HOUR, Time.hours(2));
        windowTypeMap.put(WindowType.FOUR_HOUR, Time.hours(4));
        windowTypeMap.put(WindowType.SIX_HOUR, Time.hours(6));
        windowTypeMap.put(WindowType.EIGHT_HOUR, Time.hours(8));
        windowTypeMap.put(WindowType.TWELVE_HOUR, Time.hours(12));
        windowTypeMap.put(WindowType.ONE_DAY, Time.days(1));
        windowTypeMap.put(WindowType.THREE_DAY, Time.days(3));
        windowTypeMap.put(WindowType.ONE_WEEK, Time.days(7));
        windowTypeMap.put(WindowType.ONE_MONTH, Time.days(30));
        return windowTypeMap;
    }
}
