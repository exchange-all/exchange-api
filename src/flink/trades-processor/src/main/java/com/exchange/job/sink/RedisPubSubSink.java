package com.exchange.job.sink;

import com.exchange.job.config.RedisClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.redisson.api.RedissonClient;

/**
 * @author uuhnaut69
 */
public class RedisPubSubSink<T> extends RichSinkFunction<T> {

    private transient RedissonClient client;
    private transient ObjectMapper objectMapper;
    private final String topic;

    public RedisPubSubSink(String topic) {
        this.topic = topic;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        this.client = RedisClient.getInstance(
                parameters.getString("redis.host", "localhost"),
                parameters.getInteger("redis.port", 6379)
        );
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void invoke(T value, Context context) throws Exception {
        this.client.getTopic(topic).publish(this.objectMapper.writeValueAsString(value));
    }

    @Override
    public void close() throws Exception {
        super.close();
        this.client.shutdown();
    }
}
