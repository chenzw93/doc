package com.haier.log.helper;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DistributedLock {
    private static final String LOCK = "es-operation-log-index";
    @Resource
    private RedissonClient redissonClient;

    public RLock getLock() {
        return redissonClient.getLock(LOCK);
    }
}
