package com.haier.log.configuration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class RedissonConfiguration {
    @Resource
    private RedisProperties redisProperties;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(redisProperties.getDatabase())
                .setPassword(redisProperties.getPassword())
                .setAddress(redisProperties.getUrl())
                .setTimeout(timeout);
        return Redisson.create(config);
    }
}
