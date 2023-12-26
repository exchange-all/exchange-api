package com.exchange.job.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

/**
 * @author uuhnaut69
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisClient {

    private static RedissonClient redissonClient;


    public static synchronized RedissonClient getInstance(String redisHost, int redisPort) {
        if (redissonClient == null) {
            var config = new Config();
            config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);
            config.setCodec(new StringCodec());
            redissonClient = Redisson.create(config);
        }
        return redissonClient;
    }
}
